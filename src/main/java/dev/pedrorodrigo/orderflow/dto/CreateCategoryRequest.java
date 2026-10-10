package dev.pedrorodrigo.orderflow.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateCategoryRequest(

        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "description is required")
        String description
) {}
