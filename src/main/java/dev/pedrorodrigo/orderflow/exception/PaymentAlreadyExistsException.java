package dev.pedrorodrigo.orderflow.exception;

public class PaymentAlreadyExistsException extends RuntimeException {
    public PaymentAlreadyExistsException(String message) {
        super(message);
    }
}
