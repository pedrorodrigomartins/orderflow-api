package dev.pedrorodrigo.orderflow.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductResponse(
        Long id,
        String name,
        String sku,
        String description,
        Integer stockQuantity,
        BigDecimal price,
        boolean active,
        Long categoryId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}