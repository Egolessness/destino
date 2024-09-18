/*
 * Copyright (c) 2023 by Kang Wang. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.egolessness.destino.registration.healthy;

import org.egolessness.destino.common.constant.CommonConstants;
import org.egolessness.destino.common.constant.InstanceMetadataKey;
import org.egolessness.destino.common.enumeration.RequestSchema;
import org.egolessness.destino.common.utils.PredicateUtils;
import org.egolessness.destino.registration.RegistrationExecutors;
import org.egolessness.destino.registration.model.Registration;
import org.egolessness.destino.registration.setting.ClientSetting;
import org.egolessness.destino.registration.support.RegistrationSupport;
import com.google.inject.Inject;
import org.egolessness.destino.common.fixedness.Callback;
import org.egolessness.destino.common.model.ServiceInstance;
import org.egolessness.destino.common.support.CallbackSupport;
import org.egolessness.destino.common.support.InstanceSupport;
import org.egolessness.destino.core.infrastructure.undertake.Undertaker;
import org.egolessness.destino.core.support.SystemExtensionSupport;
import org.apache.commons.lang.math.IntRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.UnresolvedAddressException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;

/**
 * tcp health check.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class TcpHealthCheck implements HealthCheck, Runnable {

    private static final Logger logger = LoggerFactory.getLogger(TcpHealthCheck.class);

    private static final Duration WAIT_TIME_LIMIT = Duration.ofMillis(500);

    private static final Duration CONNECT_TIMEOUT = Duration.ofMillis(500);

    private static final int SELECTOR_CONNECT_NUM = SystemExtensionSupport.getAvailableProcessors(0.5);

    private final Map<HealthCheckContext, CheckInfo> checkingStore = new ConcurrentHashMap<>(64);

    private final LinkedBlockingQueue<HealthCheckContext> checkedQueue = new LinkedBlockingQueue<>();

    private final Selector selector = Selector.open();

    private final HealthCheckHandler checkHandler;

    private final Undertaker undertaker;

    private final ClientSetting clientSetting;

    @Inject
    public TcpHealthCheck(final HealthCheckHandler checkHandler, final Undertaker undertaker,
                          final ClientSetting clientSetting) throws IOException {
        this.checkHandler = checkHandler;
        this.undertaker = undertaker;
        this.clientSetting = clientSetting;
        RegistrationExecutors.HEALTH_CHECK_TCP_WORKER.schedule(this, CONNECT_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean predicate(HealthCheckContext context) {
        return undertaker.isCurrent(context.getRegistrationKey());
    }

    @Override
    public void check(final HealthCheckContext context, Callback<Long> callback) {
        long nextHeartbeatTimeoutDelay = getNextHeartbeatTimeoutDelay(context);
        if (nextHeartbeatTimeoutDelay > 0) {
            CallbackSupport.triggerResponse(callback, nextHeartbeatTimeoutDelay);
            return;
        }
        checkingStore.computeIfAbsent(context, key -> {
            checkedQueue.offer(context);
            return new CheckInfo(callback);
        });
    }

    @Override
    public void cancel(HealthCheckContext context) {
        checkingStore.remove(context);
    }

    private long getNextHeartbeatTimeoutDelay(HealthCheckContext context) {
        return InstanceSupport.getHeartbeatTimeout(context.getRegistration().getInstance()).toMillis()
                + context.getBeatInfo().getLastBeat() - System.currentTimeMillis();
    }

    @Override
    public void run() {
        for (;;) {
            try {
                submitConnection();

                Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
                while (iter.hasNext()) {
                    keyStateHandle(iter.next());
                    iter.remove();
                }

                if (checkedQueue.isEmpty()) {
                    RegistrationExecutors.HEALTH_CHECK_TCP_WORKER.schedule(this, CONNECT_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
                    return;
                }
            } catch (Throwable e) {
                logger.error("[TCP-health-checker] An error occurred while establish connection of instances.", e);
                RegistrationExecutors.HEALTH_CHECK_TCP_WORKER.schedule(this, CONNECT_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
                return;
            }
        }
    }

    private void submitConnection() throws Exception {
        int processLimit = SELECTOR_CONNECT_NUM * 64;

        HealthCheckContext first = checkedQueue.poll(CONNECT_TIMEOUT.toMillis() / 2, TimeUnit.MILLISECONDS);
        if (first == null) {
            return;
        }

        List<HealthCheckContext> contexts = new ArrayList<>(processLimit);
        contexts.add(first);
        checkedQueue.drainTo(contexts, -- processLimit);

        List<Callable<CheckInfo>> callables = new ArrayList<>(contexts.size());
        for (HealthCheckContext context : contexts) {
            long nextHeartbeatTimeoutDelay = getNextHeartbeatTimeoutDelay(context);
            if (nextHeartbeatTimeoutDelay > 0) {
                terminatedCheck(context, nextHeartbeatTimeoutDelay);
                continue;
            }
            if (context.isCancelled()) {
                continue;
            }
            if (undertaker.isCurrent(context.getRegistrationKey().toString())) {
                callables.add(() -> tcpConnect(context));
            } else  {
                terminatedCheck(context, -1);
            }
        }

        RegistrationExecutors.HEALTH_CHECK_TCP_EXECUTE.invokeAll(callables);
    }

    private CheckInfo tcpConnect(final HealthCheckContext context) {
        CheckInfo checkInfo = checkingStore.get(context);
        if (checkInfo == null) {
            return null;
        }

        ServiceInstance instance = context.getRegistration().getInstance();
        SocketChannel channel = null;
        try {
            long waitTime = System.currentTimeMillis() - checkInfo.startTime;
            if (waitTime > WAIT_TIME_LIMIT.toMillis()) {
                String instanceInfo = RegistrationSupport.getInstanceInfo(context.getRegistrationKey().getInstanceKey());
                logger.warn("[TCP-health-checker] Wait {} millis is too long for instance {}.", waitTime, instanceInfo);
            }

            if (checkInfo.keyValid()) {
                checkInfo.key.cancel();
                checkInfo.key.channel().close();
            }

            channel = SocketChannel.open();
            channel.configureBlocking(false);
            channel.socket().setReuseAddress(true);
            channel.socket().setKeepAlive(true);
            channel.socket().setTcpNoDelay(true);
            channel.socket().setSoLinger(false, -1);
            channel.connect(buildSocketAddress(instance));

            SelectionKey key = channel.register(selector, SelectionKey.OP_CONNECT | SelectionKey.OP_READ);
            key.attach(context);
            checkInfo.key = key;
            checkInfo.connectTime = System.currentTimeMillis();

            RegistrationExecutors.HEALTH_CHECK_TCP_EXECUTE.schedule(() -> timeoutHandle(key),
                    CONNECT_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
        } catch (UnresolvedAddressException e) {
            String instanceInfo = RegistrationSupport.getInstanceInfo(context.getRegistrationKey().getInstanceKey());
            logger.warn("[TCP-health-checker] Unresolved address for the instance {}.", instanceInfo);
            failedCheck(context, true);
        } catch (Exception e) {
            String instanceInfo = RegistrationSupport.getInstanceInfo(context.getRegistrationKey().getInstanceKey());
            logger.warn("[TCP-health-checker] An error occurred while connect to instance {}.", instanceInfo, e);
            terminatedCheck(context);
            if (channel != null) {
                try {
                    channel.close();
                } catch (Exception se) {
                    logger.warn("[TCP-health-checker] Failed to close socket channel.", se);
                }
            }
        }

        return checkInfo;
    }

    public InetSocketAddress buildSocketAddress(ServiceInstance instance) {
        Map<String, String> metadata = instance.getMetadata();
        if (metadata == null) {
            return new InetSocketAddress(instance.getIp(), instance.getPort());
        }
        String checkUrl = metadata.get(InstanceMetadataKey.HEALTH_CHECK_URL);
        if (PredicateUtils.isBlank(checkUrl)) {
            return new InetSocketAddress(instance.getIp(), instance.getPort());
        }

        if (!checkUrl.contains(CommonConstants.PROTOCOL_SIGN)) {
            checkUrl += RequestSchema.HTTP.getPrefix();
        }
        try {
            URI uri = URI.create(checkUrl);
            return new InetSocketAddress(uri.getHost(), uri.getPort());
        } catch (Exception e) {
            return new InetSocketAddress(instance.getIp(), instance.getPort());
        }
    }

    public void keyStateHandle(final SelectionKey key) {
        HealthCheckContext context = (HealthCheckContext) key.attachment();
        SocketChannel channel = (SocketChannel) key.channel();
        try {
            CheckInfo checkInfo = checkingStore.get(context);

            if (Objects.isNull(checkInfo)) {
                return;
            }

            if (System.currentTimeMillis() - checkInfo.startTime > CONNECT_TIMEOUT.toMillis() * 10) {
                key.cancel();
                key.channel().close();
                terminatedCheck(context);
                return;
            }

            if (key.isValid() && key.isConnectable()) {
                channel.finishConnect();
                succeedCheck(context);
            }

            if (key.isValid() && key.isReadable()) {
                ByteBuffer buffer = ByteBuffer.allocate(128);
                if (channel.read(buffer) == -1) {
                    key.cancel();
                    key.channel().close();
                } else {
                    logger.warn("[TCP-health-checker] Check ok, but the connected client has response, connection unable to closed.");
                }
            }
        } catch (ConnectException e) {
            String instanceInfo = RegistrationSupport.getInstanceInfo(context.getRegistrationKey().getInstanceKey());
            logger.warn("[TCP-health-checker] Failed to connect instance {}.", instanceInfo, e);
            failedCheck(context, true);
        } catch (Exception e) {
            String instanceInfo = RegistrationSupport.getInstanceInfo(context.getRegistrationKey().getInstanceKey());
            logger.error("[TCP-health-checker] An error occurred while handle connection of instance {}.", instanceInfo, e);
            failedCheck(context, false);
            try {
                key.cancel();
                key.channel().close();
            } catch (Exception ignore) {
            }
        }
    }

    public void timeoutHandle(final SelectionKey key) {
        if (key != null && key.isValid()) {
            SocketChannel channel = (SocketChannel) key.channel();
            HealthCheckContext context = (HealthCheckContext) key.attachment();

            if (channel.isConnected()) {
                return;
            }

            failedCheck(context, false);
            try {
                key.cancel();
                channel.finishConnect();
                channel.close();
            } catch (Exception ignore) {
            }
        }
    }

    private void terminatedCheck(final HealthCheckContext context) {
        terminatedCheck(context, successDelayMillis(context.getRegistration()));
    }

    private void terminatedCheck(final HealthCheckContext context, long delayMillis) {
        CheckInfo removed = checkingStore.remove(context);
        if (removed != null) {
            CallbackSupport.triggerResponse(removed.callback, delayMillis);
        }
    }

    private void succeedCheck(final HealthCheckContext context) {
        CheckInfo checkInfo = checkingStore.remove(context);
        checkHandler.onSuccess(context);

        if (checkInfo != null) {
            CallbackSupport.triggerResponse(checkInfo.callback, successDelayMillis(context.getRegistration()));
        }
    }

    private void failedCheck(final HealthCheckContext context, final boolean aloha) {
        CheckInfo checkInfo = checkingStore.remove(context);
        checkHandler.onFail(context, aloha);

        if (checkInfo != null) {
            CallbackSupport.triggerResponse(checkInfo.callback, failedDelayMillis(context.getRegistration()));
        }
    }

    private long successDelayMillis(Registration registration) {
        long heartbeatInterval = InstanceSupport.getHeartbeatInterval(registration.getInstance()).toMillis();
        long heartbeatTimeout = InstanceSupport.getHeartbeatTimeout(registration.getInstance()).toMillis();
        long firstRandomDelayMillis = ThreadLocalRandom.current().nextLong(heartbeatInterval, heartbeatTimeout);
        return ThreadLocalRandom.current().nextLong(heartbeatInterval, firstRandomDelayMillis);
    }

    private long failedDelayMillis(Registration registration) {
        long heartbeatInterval = InstanceSupport.getHeartbeatInterval(registration.getInstance()).toMillis();
        IntRange range = clientSetting.getHealthCheckFailedDelayRange();
        int init = range.getMinimumInteger();
        int limit = range.getMaximumInteger();

        if (limit <= init) {
            return heartbeatInterval + init;
        }

        int randomDelayMillis = ThreadLocalRandom.current().nextInt(init, limit);
        return heartbeatInterval + ThreadLocalRandom.current().nextInt(init, randomDelayMillis);
    }

    private static class CheckInfo {

        long startTime = System.currentTimeMillis();

        long connectTime;

        SelectionKey key;

        Callback<Long> callback;

        boolean keyValid() {
            return Objects.nonNull(key) && key.isValid();
        }

        public CheckInfo(Callback<Long> callback) {
            this.callback = callback;
        }
    }

}
