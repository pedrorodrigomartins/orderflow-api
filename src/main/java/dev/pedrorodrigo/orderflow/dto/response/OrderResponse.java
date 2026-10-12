package dev.pedrorodrigo.orderflow.dto.response;

import dev.pedrorodrigo.orderflow.domain.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(

        Long id,
        BigDecimal totalAmount,
        OrderStatus status,
        Long customerId,
        List<OrderItemResponse> items,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {}