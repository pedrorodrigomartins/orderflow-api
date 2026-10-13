package dev.pedrorodrigo.orderflow.dto.request;

import dev.pedrorodrigo.orderflow.domain.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(
        @NotNull(message = "Order status is required")
        OrderStatus status
) {}