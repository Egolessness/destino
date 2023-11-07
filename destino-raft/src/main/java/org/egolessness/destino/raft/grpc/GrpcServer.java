package org.egolessness.destino.raft.grpc;

import com.alipay.sofa.jraft.rpc.*;
import com.alipay.sofa.jraft.rpc.impl.ConnectionClosedEventListener;
import com.alipay.sofa.jraft.rpc.impl.MarshallerRegistry;
import com.alipay.sofa.jraft.rpc.impl.RemoteAddressInterceptor;
import com.alipay.sofa.jraft.util.ExecutorServiceHelper;
import com.alipay.sofa.jraft.util.NamedThreadFactory;
import com.alipay.sofa.jraft.util.Requires;
import com.alipay.sofa.jraft.util.ThreadPoolUtil;
import org.egolessness.destino.raft.properties.DefaultConstants;
import org.egolessness.destino.raft.properties.ExecutorProperties;
import com.google.protobuf.Message;
import org.egolessness.destino.core.DestinoServer;
import io.grpc.*;
import io.grpc.protobuf.ProtoUtils;
import io.grpc.stub.ServerCalls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * grpc server.
 */
public class GrpcServer implements RpcServer {

    private static final Logger LOG = LoggerFactory.getLogger(GrpcServer.class);

    private static final String EXECUTOR_NAME = "grpc-default-executor";

    private final int port;
    private final Map<String, Message> parserClasses;
    private final MarshallerRegistry marshallerRegistry;
    private final List<ServerInterceptor> serverInterceptors = new CopyOnWriteArrayList<>();
    private final List<ConnectionClosedEventListener> closedEventListeners = new CopyOnWriteArrayList<>();
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final ExecutorService defaultExecutor;
    private final DestinoServer destinoServer;

    public GrpcServer(DestinoServer destinoServer, int port, Map<String, Message> parserClasses,
                      MarshallerRegistry marshallerRegistry, ExecutorProperties executorProperties) {
        Requires.requireTrue(port > 0 && port < 0xFFFF, "port out of range:" + port);

        this.port = port;
        this.destinoServer = destinoServer;
        this.parserClasses = parserClasses;
        this.marshallerRegistry = marshallerRegistry;
        this.defaultExecutor = ThreadPoolUtil.newBuilder()
                .poolName(EXECUTOR_NAME)
                .enableMetric(true)
                .coreThreads(Math.min(20, getProcessorThreads(executorProperties) / 5))
                .maximumThreads(getProcessorThreads(executorProperties))
                .keepAliveSeconds(60L)
                .workQueue(new SynchronousQueue<>())
                .threadFactory(new NamedThreadFactory(EXECUTOR_NAME + "-", true))
                .rejectedHandler((r, executor) -> {
                    throw new RejectedExecutionException("[" + EXECUTOR_NAME + "], task " + r.toString() +
                            " rejected from " +
                            executor.toString());
                })
                .build();

        registerDefaultServerInterceptor();
    }

    private int getProcessorThreads(ExecutorProperties executorProperties) {
        Integer processorThreads = executorProperties.getProcessorThreads();
        if (processorThreads == null || processorThreads <= 0) {
            return DefaultConstants.DEFAULT_PROCESSOR_THREADS;
        }
        return processorThreads;
    }

    @Override
    public boolean init(final Void opts) {
        if (!this.started.compareAndSet(false, true)) {
            throw new IllegalStateException("grpc server has started");
        }
        return true;
    }

    @Override
    public void shutdown() {
        if (!this.started.compareAndSet(true, false)) {
            return;
        }
        ExecutorServiceHelper.shutdownAndAwaitTermination(this.defaultExecutor);
    }

    @Override
    public void registerConnectionClosedEventListener(final ConnectionClosedEventListener listener) {
        this.closedEventListeners.add(listener);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void registerProcessor(final RpcProcessor processor) {
        final String interest = processor.interest();
        final Message reqIns = Requires.requireNonNull(this.parserClasses.get(interest), "null default instance: " + interest);
        final MethodDescriptor<Message, Message> method = MethodDescriptor.<Message, Message>newBuilder()
                .setType(MethodDescriptor.MethodType.UNARY)
                .setFullMethodName(MethodDescriptor.generateFullMethodName(processor.interest(), GrpcRaftRpcFactory.FIXED_METHOD_NAME))
                .setRequestMarshaller(ProtoUtils.marshaller(reqIns))
                .setResponseMarshaller(ProtoUtils.marshaller(this.marshallerRegistry.findResponseInstanceByRequest(interest)))
                .build();

        final ServerCallHandler<Message, Message> handler = ServerCalls.asyncUnaryCall(
                (request, responseObserver) -> {
                    final SocketAddress remoteAddress = RemoteAddressInterceptor.getRemoteAddress();
                    final Connection conn = JRaftConnectionHolder.getConnection(this.closedEventListeners);

                    final RpcContext rpcCtx = new RpcContext() {

                        @Override
                        public void sendResponse(final Object responseObj) {
                            try {
                                responseObserver.onNext((Message) responseObj);
                                responseObserver.onCompleted();
                            } catch (final Throwable t) {
                                LOG.warn("[GRPC] failed to send response.", t);
                            }
                        }

                        @Override
                        public Connection getConnection() {
                            return conn;
                        }

                        @Override
                        public String getRemoteAddress() {
                            return remoteAddress != null ? remoteAddress.toString() : null;
                        }
                    };

                    final RpcProcessor.ExecutorSelector selector = processor.executorSelector();
                    Executor executor;
                    if (selector != null && request instanceof RpcRequests.AppendEntriesRequest) {
                        final RpcRequests.AppendEntriesRequest req = (RpcRequests.AppendEntriesRequest) request;
                        final RpcRequests.AppendEntriesRequestHeader.Builder header = RpcRequests.AppendEntriesRequestHeader
                                .newBuilder()
                                .setGroupId(req.getGroupId())
                                .setPeerId(req.getPeerId())
                                .setServerId(req.getServerId());
                        executor = selector.select(interest, header.build());
                    } else {
                        executor = processor.executor();
                    }

                    if (executor == null) {
                        executor = this.defaultExecutor;
                    }

                    if (executor != null) {
                        executor.execute(() -> processor.handleRequest(rpcCtx, request));
                    } else {
                        processor.handleRequest(rpcCtx, request);
                    }
                });

        final ServerServiceDefinition serviceDef = ServerServiceDefinition.builder(interest).addMethod(method, handler).build();
        destinoServer.addGrpcService(port, ServerInterceptors.intercept(serviceDef, this.serverInterceptors.toArray(new ServerInterceptor[0])));
    }

    @Override
    public int boundPort() {
        return port;
    }

    private void registerDefaultServerInterceptor() {
        this.serverInterceptors.add(new RemoteAddressInterceptor());
    }
}
