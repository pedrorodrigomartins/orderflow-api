package dev.pedrorodrigo.orderflow.exception;

public class PaymentExpiredException extends RuntimeException {
    public PaymentExpiredException(String message) {
        super(message);
    }
}
