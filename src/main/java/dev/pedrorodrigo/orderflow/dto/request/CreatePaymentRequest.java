package dev.pedrorodrigo.orderflow.dto.request;

import dev.pedrorodrigo.orderflow.domain.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public record CreatePaymentRequest(

        @NotNull
        Long orderId,

        @NotNull
        PaymentMethod paymentMethod,

        @NotNull
        @Positive
        Integer installments,

        @NotNull
        LocalDateTime expiresAt

) {}