package dev.buskopan.rinha_backend_2025;

import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class PaymentService {
    private final WebClient webClient;
    private final DatabaseClient db;

    private static final String DEFAULT_URL  = "http://payment-processor-default:8080";
    private static final String FALLBACK_URL = "http://payment-processor-fallback:8080";

    public PaymentService(WebClient webClient, DatabaseClient db) {
        this.webClient = webClient;
        this.db        = db;
    }

    public Mono<Void> process(PaymentRequest req) {
        return callProcessor(DEFAULT_URL, req)
                .onErrorResume(err -> callProcessor(FALLBACK_URL, req))
                .then();
    }

    private Mono<Void> callProcessor(String baseUrl, PaymentRequest req) {
        Map<String,Object> payload = Map.of(
                "correlationId", req.correlationId(),
                "amount",        req.amount(),
                "requestedAt",   LocalDateTime.now().toString()
        );

        return webClient.post()
                .uri(baseUrl + "/payments")
                .bodyValue(payload)
                .retrieve()
                .onStatus(status -> !status.is2xxSuccessful(),
                        resp -> Mono.error(new RuntimeException("HTTP " + resp.statusCode())))
                .bodyToMono(String.class)
                .flatMap(respBody -> insertDB(req, baseUrl.contains("fallback") ? "fallback" : "default")).then();
    }

    private Mono<Long> insertDB(PaymentRequest req, String processor) {
        LocalDateTime now = LocalDateTime.now();
        return db.sql("""
                INSERT INTO payments(correlationId, amount, requested_at, processor)
                VALUES($1, $2, $3, $4)
            """)
                .bind(0, req.correlationId())
                .bind(1, req.amount())
                .bind(2, now)
                .bind(3, processor)
                .fetch()
                .rowsUpdated();
    }

    public Mono<HealthResponse> checkDefault() {
        return webClient.get()
                .uri(DEFAULT_URL + "/payments/service-health")
                .retrieve()
                .bodyToMono(HealthResponse.class);
    }

    public Mono<HealthResponse> checkFallback() {
        return webClient.get()
                .uri(FALLBACK_URL + "/payments/service-health")
                .retrieve()
                .bodyToMono(HealthResponse.class)
                .onErrorReturn(new HealthResponse(true, -1));
    }
}
