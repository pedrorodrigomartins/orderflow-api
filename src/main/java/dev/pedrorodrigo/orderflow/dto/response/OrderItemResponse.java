package dev.pedrorodrigo.orderflow.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderItemResponse(

        Long id,
        Long productId,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {}