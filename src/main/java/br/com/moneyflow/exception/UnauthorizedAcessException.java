package br.com.moneyflow.exception;

public class UnauthorizedAcessException extends RuntimeException {
    public UnauthorizedAcessException(String message) {
        super(message);
    }
}
