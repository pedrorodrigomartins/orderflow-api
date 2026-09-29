package dev.pedrorodrigo.orderflow.dto;

import java.time.LocalDateTime;

public record CustomerResponse(
        Long id,
        String name,
        String email,
        LocalDateTime createdAt
) {}
