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

package com.egolessness.destino.core;

import com.egolessness.destino.core.infrastructure.PortGetter;
import com.egolessness.destino.core.properties.CorsProperties;
import com.egolessness.destino.core.properties.ServerProperties;
import com.egolessness.destino.core.properties.constants.DefaultConstants;
import com.egolessness.destino.core.support.PropertiesSupport;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.linecorp.armeria.common.HttpMethod;
import com.linecorp.armeria.common.grpc.GrpcMeterIdPrefixFunction;
import com.linecorp.armeria.common.metric.MeterIdPrefixFunction;
import com.linecorp.armeria.server.Server;
import com.linecorp.armeria.server.ServerBuilder;
import com.linecorp.armeria.server.cors.CorsServiceBuilder;
import com.linecorp.armeria.server.logging.AccessLogWriter;
import com.linecorp.armeria.server.metric.MetricCollectingService;
import com.egolessness.destino.common.fixedness.Lucermaire;
import com.egolessness.destino.common.exception.DestinoException;
import com.egolessness.destino.common.utils.PredicateUtils;
import com.linecorp.armeria.common.grpc.GrpcJsonMarshaller;
import com.linecorp.armeria.common.grpc.GrpcSerializationFormats;
import com.linecorp.armeria.server.*;
import com.linecorp.armeria.server.cors.CorsService;
import com.linecorp.armeria.server.grpc.GrpcService;
import com.linecorp.armeria.server.grpc.GrpcServiceBuilder;
import io.grpc.*;
import io.grpc.protobuf.services.ProtoReflectionService;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * destino server.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@SuppressWarnings("SpellCheckingInspection")
@Singleton
public class DestinoServer implements Lucermaire {

    private final Map<Integer, ServerCompose> serverStore = new ConcurrentHashMap<>(4);

    private final ServerProperties serverProperties;

    private final CorsProperties corsProperties;

    private final PortGetter portGetter;

    private final int defaultPort;

    @Inject
    public DestinoServer(ServerProperties serverProperties, CorsProperties corsProperties, PortGetter portGetter) {
        this.serverProperties = serverProperties;
        this.corsProperties = corsProperties;
        this.portGetter = portGetter;
        this.defaultPort = portGetter.getOuterPort();
    }

    public synchronized GrpcServiceBuilder getGrpcServiceBuilder(final int port) {
        ServerCompose serverCompose = serverStore.computeIfAbsent(port, key -> new ServerCompose());
        if (Objects.isNull(serverCompose.grpcServiceBuilder)) {
            serverCompose.grpcServiceBuilder = initGrpcServiceBuilder();
        }
        return serverCompose.grpcServiceBuilder;
    }

    private GrpcServiceBuilder initGrpcServiceBuilder() {
        return GrpcService.builder()
                .maxRequestMessageLength((int) serverProperties.getMaxRequestLength())
                .jsonMarshallerFactory(any -> GrpcJsonMarshaller.ofGson())
                .addService(ProtoReflectionService.newInstance())
                .supportedSerializationFormats(GrpcSerializationFormats.values())
                .enableUnframedRequests(true)
                .compressorRegistry(CompressorRegistry.getDefaultInstance())
                .decompressorRegistry(DecompressorRegistry.getDefaultInstance());
    }

    public synchronized ServerBuilder getServerBuilder(final int port) {
        return serverStore.computeIfAbsent(port, key -> new ServerCompose()).serverBuilder;
    }

    public void addGrpcService(final BindableService bindableService) {
        getGrpcServiceBuilder(defaultPort).addService(bindableService);
    }

    public void addGrpcService(final int port, final BindableService bindableService) {
        getGrpcServiceBuilder(port).addService(bindableService);
    }

    public void addGrpcService(final int port, final ServerServiceDefinition serverServiceDefinition) {
        getGrpcServiceBuilder(port).addService(serverServiceDefinition);
    }

    public void addGrpcIntercept(final ServerInterceptor interceptor) {
        getGrpcServiceBuilder(defaultPort).intercept(interceptor);
    }

    public void addHttpService(final String prefix, final HttpService httpService) {
        getServerBuilder(defaultPort).serviceUnder(prefix, httpService);
    }

    public void addHttpAnnotatedService(final Object restService) {
        getServerBuilder(defaultPort).annotatedService(restService);
    }

    public void addRestDecorator(final DecoratingHttpServiceFunction decoratingHttpServiceFunction) {
        serverStore.computeIfAbsent(defaultPort, key -> new ServerCompose()).decorators.add(decoratingHttpServiceFunction);
    }

    public CompletableFuture<Void> start(final int port) {
        ServerCompose compose = serverStore.get(port);
        return compose.start(port);
    }

    public CompletableFuture<Void> shutdownAll() {
        CompletableFuture<?>[] completableFutures = serverStore.values().stream().map(ServerCompose::stop).
                collect(Collectors.toList()).toArray(new CompletableFuture<?>[serverStore.size()]);
        return CompletableFuture.allOf(completableFutures);
    }

    public CompletableFuture<Void> startAll() {
        CompletableFuture<?>[] completableFutures = serverStore.entrySet().stream()
                .map(entry -> entry.getValue().start(entry.getKey()))
                .toArray(CompletableFuture<?>[]::new);
        return CompletableFuture.allOf(completableFutures);
    }

    @Override
    public void shutdown() throws DestinoException {
        shutdownAll().join();
    }

    class ServerCompose {

        Server server;

        ServerBuilder serverBuilder = Server.builder();

        GrpcServiceBuilder grpcServiceBuilder;

        AtomicBoolean started = new AtomicBoolean();

        List<DecoratingHttpServiceFunction> decorators = new ArrayList<>();

        private ServerCompose() {}

        private Server buildServer(final int port) {

            if (Objects.nonNull(server)) {
                return server;
            }

            if (Objects.nonNull(grpcServiceBuilder)) {
                GrpcService grpcService = grpcServiceBuilder.build();
                serverBuilder.service(grpcService)
                        .decorator(MetricCollectingService.newDecorator(GrpcMeterIdPrefixFunction.of("grpc.service")))
                        .requestTimeoutMillis(serverProperties.getIdleTimeout());
            }

            if (serverProperties.isAccessLog()) {
                serverBuilder.accessLogWriter(AccessLogWriter.combined(), true);
            }

            String serverScope;
            if (portGetter.getInnerPort() == port && portGetter.getOuterPort() == port) {
                serverScope = "INNER and OUTER";
            } else if (portGetter.getInnerPort() == port) {
                serverScope = "INNER";
            } else if (portGetter.getOuterPort() == port) {
                serverScope = "OUTER";
            } else {
                serverScope = "OTHER";
            }

            ServerListener listener = ServerListener.builder()
                    .whenStarted(srv -> Loggers.SERVER.info("The destino server of {} started on port: {}", serverScope, srv.activeLocalPort()))
                    .whenStopped(srv -> Loggers.SERVER.info("The destino server of {} stopped on port: {}", serverScope, srv.activeLocalPort()))
                    .build();

            serverBuilder.idleTimeoutMillis(serverProperties.getIdleTimeout())
                    .requestTimeoutMillis(serverProperties.getRequestTimeout())
                    .maxRequestLength(serverProperties.getMaxRequestLength())
                    .maxNumConnections(serverProperties.getMaxNumConnections())
                    .http1MaxHeaderSize(serverProperties.getHttpMaxHeaderSize())
                    .http1MaxChunkSize(serverProperties.getHttpMaxChunkSize())
                    .http1MaxInitialLineLength(serverProperties.getHttpMaxInitialLineLength())
                    .http2MaxHeaderListSize(serverProperties.getHttp2MaxHeaderListSize())
                    .http2InitialConnectionWindowSize(serverProperties.getHttp2InitialConnectionWindowSize())
                    .http2MaxFrameSize(serverProperties.getHttp2MaxFrameSize())
                    .http2InitialStreamWindowSize(serverProperties.getHttp2InitialStreamWindowSize())
                    .http2MaxStreamsPerConnection(serverProperties.getHttp2MaxStreamsPerConnection())
                    .serverListener(listener)
                    .decorator(buildCorsDecorator(corsProperties))
                    .decorator(MetricCollectingService.newDecorator(MeterIdPrefixFunction.ofDefault("http.service")))
                    .http(port);

            String contextPath = PropertiesSupport.getStandardizeContextPath(serverProperties);
            if (PredicateUtils.isNotBlank(contextPath)) {
                serverBuilder.baseContextPath(contextPath);
            }

            return server = serverBuilder.build();
        }

        private CompletableFuture<Void> start(int port) {
            if (started.compareAndSet(false, true)) {
                if (Objects.isNull(server)) {
                    server = buildServer(port);
                }
                return server.start();
            }
            return CompletableFuture.completedFuture(null);
        }

        private CompletableFuture<Void> stop() {
            if (Objects.nonNull(server) && started.compareAndSet(true, false)) {
                return server.stop();
            }
            return CompletableFuture.completedFuture(null);
        }
    }

    private Function<? super HttpService, CorsService> buildCorsDecorator(CorsProperties corsProperties) {

        CorsServiceBuilder corsServiceBuilder;
        if (Objects.equals(corsProperties.getAllowedOrigins(), DefaultConstants.DEFAULT_CORS_ALLOWED_ANY)) {
            corsServiceBuilder = CorsService.builderForAnyOrigin();
        } else {
            corsServiceBuilder = CorsService.builder(corsProperties.getAllowedOrigins());
        }

        if (Objects.equals(corsProperties.getAllowedHeaders(), DefaultConstants.DEFAULT_CORS_ALLOWED_ANY)) {
            corsServiceBuilder.allowAllRequestHeaders(true);
        } else {
            corsServiceBuilder.allowRequestHeaders(corsProperties.getAllowedHeaders());
        }

        if (corsProperties.isAllowCredentials()) {
            corsServiceBuilder.allowCredentials();
        }

        return corsServiceBuilder
                .exposeHeaders(corsProperties.getExposedHeaders())
                .maxAge(corsProperties.getMaxAge())
                .allowRequestMethods(HttpMethod.values())
                .newDecorator();
    }

}
