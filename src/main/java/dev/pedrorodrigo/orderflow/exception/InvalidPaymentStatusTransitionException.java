package dev.pedrorodrigo.orderflow.exception;

public class InvalidPaymentStatusTransitionException extends RuntimeException {
    public InvalidPaymentStatusTransitionException(String message) {
        super(message);
    }
}
