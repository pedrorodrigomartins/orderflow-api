package dev.pedrorodrigo.orderflow.exception;

public class InvalidOrderStatusTransitionException extends RuntimeException {
    public InvalidOrderStatusTransitionException(String message) {
        super(message);
    }
}
