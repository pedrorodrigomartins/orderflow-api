package dev.pedrorodrigo.orderflow.exception;

import java.time.LocalDateTime;

public record ErrorResponse(

        Integer status,
        String error,
        String message,
        String path,
        LocalDateTime timestamp

) {}
