package dev.pedrorodrigo.orderflow.exception;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record ValidationErrorResponse(
        Integer status,
        String error,
        String message,
        String path,
        LocalDateTime timestamp,
        Map<String, List<String>> fieldErrors
) {}
