package br.com.moneyflow.exception;

public class InvalidCategoryTypeException extends RuntimeException {
    public InvalidCategoryTypeException(String message) {
        super(message);
    }
}
