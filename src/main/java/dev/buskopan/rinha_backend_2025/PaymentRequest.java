package dev.buskopan.rinha_backend_2025;

import java.util.UUID;

public record PaymentRequest(UUID correlationId, double amount) {
}
