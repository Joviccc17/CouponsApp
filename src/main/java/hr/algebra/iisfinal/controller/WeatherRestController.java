package hr.algebra.iisfinal.controller;

import hr.algebra.iisfinal.grpc.generated.CityRequest;
import hr.algebra.iisfinal.grpc.generated.WeatherServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/weather")
@Slf4j
public class WeatherRestController {

    @Value("${grpc.server.port:9090}")
    private int grpcPort;

    @GetMapping
    public List<Map<String, String>> getTemperature(@RequestParam String city) {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", grpcPort)
                .usePlaintext()
                .build();
        try {
            WeatherServiceGrpc.WeatherServiceBlockingStub stub =
                    WeatherServiceGrpc.newBlockingStub(channel);
            CityRequest req = CityRequest.newBuilder()
                    .setCityName(city)
                    .build();
            List<Map<String, String>> results = new ArrayList<>();
            stub.getTemperature(req).forEachRemaining(r -> {
                Map<String, String> entry = new HashMap<>();
                entry.put("city", r.getCity());
                entry.put("temperature", r.getTemperature());
                entry.put("timestamp", r.getTimestamp());
                results.add(entry);
            });
            return results;
        } catch (Exception e) {
            log.error("gRPC call failed", e);
            List<Map<String, String>> error = new ArrayList<>();
            Map<String, String> err = new HashMap<>();
            err.put("error", "Weather service unavailable: " + e.getMessage());
            error.add(err);
            return error;
        } finally {
            channel.shutdown();
        }
    }
}