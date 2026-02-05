package br.com.moneyflow.exception;

public class InvalidYearException extends RuntimeException {
    public InvalidYearException(String message) {
        super(message);
    }
}
