package dev.pedrorodrigo.orderflow.exception;

public class InvalidOrderCancellationException extends RuntimeException {
    public InvalidOrderCancellationException(String message) {
        super(message);
    }
}
