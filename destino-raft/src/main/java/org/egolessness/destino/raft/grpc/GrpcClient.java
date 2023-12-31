package org.egolessness.destino.raft.grpc;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import com.alipay.sofa.jraft.rpc.impl.ManagedChannelHelper;
import com.alipay.sofa.jraft.rpc.impl.MarshallerRegistry;
import org.egolessness.destino.common.utils.PredicateUtils;
import org.egolessness.destino.core.container.ContainerFactory;
import org.egolessness.destino.core.container.MemberContainer;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.MethodDescriptor;
import io.grpc.protobuf.ProtoUtils;
import io.grpc.stub.ClientCalls;
import io.grpc.stub.StreamObserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alipay.sofa.jraft.ReplicatorGroup;
import com.alipay.sofa.jraft.entity.PeerId;
import com.alipay.sofa.jraft.error.InvokeTimeoutException;
import com.alipay.sofa.jraft.error.RemotingException;
import com.alipay.sofa.jraft.option.RpcOptions;
import com.alipay.sofa.jraft.rpc.InvokeCallback;
import com.alipay.sofa.jraft.rpc.InvokeContext;
import com.alipay.sofa.jraft.rpc.RpcClient;
import com.alipay.sofa.jraft.rpc.RpcUtils;
import com.alipay.sofa.jraft.util.DirectExecutor;
import com.alipay.sofa.jraft.util.Endpoint;
import com.alipay.sofa.jraft.util.Requires;
import com.alipay.sofa.jraft.util.SystemPropertyUtil;
import com.google.protobuf.Message;

public class GrpcClient implements RpcClient {

    private static final Logger                 LOG                  = LoggerFactory.getLogger(GrpcClient.class);

    private static final int                    RESET_CONN_THRESHOLD = SystemPropertyUtil.getInt("jraft.grpc.max.conn.failures.to_reset", 2);

    private static final int                    RPC_MAX_INBOUND_MESSAGE_SIZE = SystemPropertyUtil.getInt("jraft.grpc.max_inbound_message_size.bytes", 4 * 1024 * 1024);

    private final Map<Endpoint, ManagedChannel> managedChannelPool   = new ConcurrentHashMap<>();
    private final Map<Endpoint, AtomicInteger>  transientFailures    = new ConcurrentHashMap<>();
    private final Map<String, Message>          parserClasses;
    private final MarshallerRegistry            marshallerRegistry;
    private final MemberContainer               memberContainer;
    private volatile ReplicatorGroup            replicatorGroup;

    public GrpcClient(Map<String, Message> parserClasses, MarshallerRegistry marshallerRegistry,
                      ContainerFactory containerFactory) {
        this.parserClasses = parserClasses;
        this.marshallerRegistry = marshallerRegistry;
        this.memberContainer = containerFactory.getContainer(MemberContainer.class);
    }

    @Override
    public boolean init(final RpcOptions opts) {
        // do nothing
        return true;
    }

    @Override
    public void shutdown() {
        closeAllChannels();
        this.transientFailures.clear();
    }

    @Override
    public boolean checkConnection(final Endpoint endpoint) {
        return checkConnection(endpoint, false);
    }

    @Override
    public boolean checkConnection(final Endpoint endpoint, final boolean createIfAbsent) {
        Requires.requireNonNull(endpoint, "endpoint");
        return checkChannel(endpoint, createIfAbsent);
    }

    @Override
    public void closeConnection(final Endpoint endpoint) {
        Requires.requireNonNull(endpoint, "endpoint");
        closeChannel(endpoint);
    }

    @Override
    public void registerConnectEventListener(final ReplicatorGroup replicatorGroup) {
        this.replicatorGroup = replicatorGroup;
    }

    @Override
    public Object invokeSync(final Endpoint endpoint, final Object request, final InvokeContext ctx,
                             final long timeoutMs) throws RemotingException {
        final CompletableFuture<Object> future = new CompletableFuture<>();

        invokeAsync(endpoint, request, ctx, (result, err) -> {
            if (err == null) {
                future.complete(result);
            } else {
                future.completeExceptionally(err);
            }
        }, timeoutMs);

        try {
            return future.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (final TimeoutException e) {
            future.cancel(true);
            throw new InvokeTimeoutException(e);
        } catch (final Throwable t) {
            future.cancel(true);
            throw new RemotingException(t);
        }
    }

    @Override
    public void invokeAsync(final Endpoint endpoint, final Object request, final InvokeContext ctx,
                            final InvokeCallback callback, final long timeoutMs) {
        Requires.requireNonNull(endpoint, "endpoint");
        Requires.requireNonNull(request, "request");

        final Executor executor = callback.executor() != null ? callback.executor() : DirectExecutor.INSTANCE;

        final Channel ch = getCheckedChannel(endpoint);
        if (ch == null) {
            executor.execute(() -> callback.complete(null, new RemotingException("Fail to connect: " + endpoint)));
            return;
        }

        final MethodDescriptor<Message, Message> method = getCallMethod(endpoint, request);
        final CallOptions callOpts = CallOptions.DEFAULT.withDeadlineAfter(timeoutMs, TimeUnit.MILLISECONDS);

        ClientCalls.asyncUnaryCall(ch.newCall(method, callOpts), (Message) request, new StreamObserver<Message>() {

            @Override
            public void onNext(final Message value) {
                executor.execute(() -> callback.complete(value, null));
            }

            @Override
            public void onError(final Throwable throwable) {
                executor.execute(() -> callback.complete(null, throwable));
            }

            @Override
            public void onCompleted() {
                // NO-OP
            }
        });
    }

    private MethodDescriptor<Message, Message> getCallMethod(final Endpoint endpoint, final Object request) {
        final String contextPath = memberContainer.getContextPath(endpoint.toString());
        final String interest = request.getClass().getName();
        final Message reqIns = Requires.requireNonNull(this.parserClasses.get(interest), "null default instance: "
                                                                                         + interest);

        String fullMethodName = MethodDescriptor.generateFullMethodName(interest, GrpcRaftRpcFactory.FIXED_METHOD_NAME);
        if (PredicateUtils.isNotBlank(contextPath)) {
            fullMethodName = contextPath + "/" + fullMethodName;
        }

        return MethodDescriptor
            .<Message, Message> newBuilder()
            .setType(MethodDescriptor.MethodType.UNARY)
            .setFullMethodName(fullMethodName)
            .setRequestMarshaller(ProtoUtils.marshaller(reqIns))
            .setResponseMarshaller(ProtoUtils.marshaller(this.marshallerRegistry.findResponseInstanceByRequest(interest)))
            .build();
    }

    private ManagedChannel getCheckedChannel(final Endpoint endpoint) {
        final ManagedChannel ch = getChannel(endpoint, true);

        if (checkConnectivity(endpoint, ch)) {
            return ch;
        }

        return null;
    }

    private ManagedChannel getChannel(final Endpoint endpoint, final boolean createIfAbsent) {
        if (createIfAbsent) {
            return this.managedChannelPool.computeIfAbsent(endpoint, this::newChannel);
        } else {
            return this.managedChannelPool.get(endpoint);
        }
    }

    private ManagedChannel newChannel(final Endpoint endpoint) {
        final ManagedChannel ch = ManagedChannelBuilder.forAddress(endpoint.getIp(), endpoint.getPort())
            .usePlaintext().directExecutor().maxInboundMessageSize(RPC_MAX_INBOUND_MESSAGE_SIZE)
            .build();

        LOG.debug("Creating new channel to: {}.", endpoint);

        notifyWhenStateChanged(ConnectivityState.IDLE, endpoint, ch);

        return ch;
    }

    private ManagedChannel removeChannel(final Endpoint endpoint) {
        return this.managedChannelPool.remove(endpoint);
    }

    private void notifyWhenStateChanged(final ConnectivityState state, final Endpoint endpoint, final ManagedChannel ch) {
        ch.notifyWhenStateChanged(state, () -> onStateChanged(endpoint, ch));
    }

    private void onStateChanged(final Endpoint endpoint, final ManagedChannel ch) {
        final ConnectivityState state = ch.getState(false);

        LOG.debug("The channel {} is in state: {}.", endpoint, state);

        switch (state) {
            case READY:
                notifyReady(endpoint);
                notifyWhenStateChanged(ConnectivityState.READY, endpoint, ch);
                break;
            case TRANSIENT_FAILURE:
                notifyFailure(endpoint);
                notifyWhenStateChanged(ConnectivityState.TRANSIENT_FAILURE, endpoint, ch);
                break;
            case SHUTDOWN:
                notifyShutdown(endpoint);
                break;
            case CONNECTING:
                notifyWhenStateChanged(ConnectivityState.CONNECTING, endpoint, ch);
                break;
            case IDLE:
                notifyWhenStateChanged(ConnectivityState.IDLE, endpoint, ch);
                break;
        }
    }

    private void notifyReady(final Endpoint endpoint) {
        LOG.debug("The channel {} has successfully established.", endpoint);

        clearConnFailuresCount(endpoint);

        final ReplicatorGroup rpGroup = this.replicatorGroup;
        if (rpGroup != null) {
            try {
                RpcUtils.runInThread(() -> {
                    final PeerId peer = new PeerId();
                    if (peer.parse(endpoint.toString())) {
                        LOG.debug("Peer {} is connected.", peer);
                        rpGroup.checkReplicator(peer, true);
                    } else {
                        LOG.error("Fail to parse peer: {}.", endpoint);
                    }
                });
            } catch (final Throwable t) {
                LOG.error("Fail to check replicator {}.", endpoint, t);
            }
        }
    }

    private void notifyFailure(final Endpoint endpoint) {
        LOG.warn("There has been some transient failure on this channel {}.", endpoint);
    }

    private void notifyShutdown(final Endpoint endpoint) {
        LOG.warn("This channel {} has started shutting down. Any new RPCs should fail immediately.", endpoint);
    }

    private void closeAllChannels() {
        for (final Map.Entry<Endpoint, ManagedChannel> entry : this.managedChannelPool.entrySet()) {
            final ManagedChannel ch = entry.getValue();
            LOG.info("Shutdown managed channel: {}, {}.", entry.getKey(), ch);
            ManagedChannelHelper.shutdownAndAwaitTermination(ch);
        }
        this.managedChannelPool.clear();
    }

    private void closeChannel(final Endpoint endpoint) {
        final ManagedChannel ch = removeChannel(endpoint);
        LOG.debug("Close connection: {}, {}.", endpoint, ch);
        if (ch != null) {
            ManagedChannelHelper.shutdownAndAwaitTermination(ch);
        }
    }

    private boolean checkChannel(final Endpoint endpoint, final boolean createIfAbsent) {
        final ManagedChannel ch = getChannel(endpoint, createIfAbsent);

        if (ch == null) {
            return false;
        }

        return checkConnectivity(endpoint, ch);
    }

    private int incConnFailuresCount(final Endpoint endpoint) {
        return this.transientFailures.computeIfAbsent(endpoint, ep -> new AtomicInteger()).incrementAndGet();
    }

    private void clearConnFailuresCount(final Endpoint endpoint) {
        this.transientFailures.remove(endpoint);
    }

    private boolean checkConnectivity(final Endpoint endpoint, final ManagedChannel ch) {
        final ConnectivityState st = ch.getState(false);

        if (st != ConnectivityState.TRANSIENT_FAILURE && st != ConnectivityState.SHUTDOWN) {
            return true;
        }

        final int c = incConnFailuresCount(endpoint);
        if (c < RESET_CONN_THRESHOLD) {
            if (c == RESET_CONN_THRESHOLD - 1) {
                // For sub-channels that are in TRANSIENT_FAILURE state, short-circuit the backoff timer and make
                // them reconnect immediately. May also attempt to invoke NameResolver#refresh
                ch.resetConnectBackoff();
            }
            return true;
        }

        clearConnFailuresCount(endpoint);

        final ManagedChannel removedCh = removeChannel(endpoint);

        if (removedCh == null) {
            // The channel has been removed and closed by another
            return false;
        }

        LOG.warn("Channel[{}] in [INACTIVE] state {} times, it has been removed from the pool.", endpoint, c);

        if (removedCh != ch) {
            // Now that it's removed, close it
            ManagedChannelHelper.shutdownAndAwaitTermination(removedCh, 100);
        }

        ManagedChannelHelper.shutdownAndAwaitTermination(ch, 100);

        return false;
    }

}
