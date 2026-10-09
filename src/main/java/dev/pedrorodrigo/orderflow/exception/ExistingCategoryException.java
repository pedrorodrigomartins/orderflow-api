package dev.pedrorodrigo.orderflow.exception;

public class ExistingCategoryException extends RuntimeException {
    public ExistingCategoryException(String message) {
        super(message);
    }
}
