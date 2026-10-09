package dev.pedrorodrigo.orderflow.dto;

import jakarta.persistence.Column;

public record CreateCategoryRequest(

        @Column(nullable = false, unique = true)
        String name,

        @Column(columnDefinition = "TEXT")
        String description
) {}
