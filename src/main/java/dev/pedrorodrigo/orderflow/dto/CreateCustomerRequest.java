package dev.pedrorodrigo.orderflow.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateCustomerRequest(

        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "CPF is required")
        @Pattern(regexp = "\\d{11}", message = "CPF must be 11 digits")
        String cpf,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email
) {}
