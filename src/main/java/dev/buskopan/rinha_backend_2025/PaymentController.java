package dev.buskopan.rinha_backend_2025;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Controller
@ResponseBody
public class PaymentController {

    private final PaymentService service;

    public PaymentController(PaymentService service) {
        this.service = service;
    }

    @PostMapping("/payments")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Mono<Void> process(@RequestBody PaymentRequest req) {
        return service.process(req);
    }

    @GetMapping("/payments/default")
    @ResponseStatus(HttpStatus.OK)
    public Mono<HealthResponse> checkDefault() {
        Mono<HealthResponse> healthResponse = service.checkDefault();
        System.out.println(healthResponse);
        return healthResponse;
    }

    @GetMapping("/payments/fallback")
    @ResponseStatus(HttpStatus.OK)
    public Mono<HealthResponse> checkFallback() {
        return service.checkFallback();
    }
}
