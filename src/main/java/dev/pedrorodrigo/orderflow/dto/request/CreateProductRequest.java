package dev.pedrorodrigo.orderflow.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreateProductRequest(

        @NotBlank
        @Size(max = 100)
        String name,

        @NotBlank
        @Size(max = 50)
        String sku,

        String description,

        @NotNull
        @Min(0)
        Integer stockQuantity,

        @NotNull
        @DecimalMin("0.01")
        BigDecimal price,

        @NotNull
        Long categoryId

) {}