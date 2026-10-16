package dev.pedrorodrigo.orderflow.dto.response;

import dev.pedrorodrigo.orderflow.domain.enums.PaymentMethod;
import dev.pedrorodrigo.orderflow.domain.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(

        Long id,
        Long orderId,
        BigDecimal amount,
        PaymentMethod paymentMethod,
        PaymentStatus status,
        Integer installments,
        LocalDateTime expiresAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {}