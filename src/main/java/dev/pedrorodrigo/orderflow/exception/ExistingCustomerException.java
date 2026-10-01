package dev.pedrorodrigo.orderflow.exception;

public class ExistingCustomerException extends RuntimeException {
    public ExistingCustomerException(String message) {
        super(message);
    }
}
