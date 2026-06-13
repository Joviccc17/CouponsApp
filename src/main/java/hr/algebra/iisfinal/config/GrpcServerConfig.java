package hr.algebra.iisfinal.config;

import hr.algebra.iisfinal.grpc.WeatherGrpcService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class GrpcServerConfig {

    @Value("${grpc.server.port:9090}")
    private int grpcPort;

    private final WeatherGrpcService weatherGrpcService;

    private Server grpcServer;

    @PostConstruct
    public void startGrpcServer() throws IOException {
        grpcServer = ServerBuilder.forPort(grpcPort)
                .addService(weatherGrpcService)
                .build()
                .start();
        log.info("gRPC server started on port {}", grpcPort);
    }

    @PreDestroy
    public void stopGrpcServer() {
        if (grpcServer != null) {
            grpcServer.shutdown();
            log.info("gRPC server stopped");
        }
    }
}