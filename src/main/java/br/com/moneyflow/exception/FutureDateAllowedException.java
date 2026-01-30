package br.com.moneyflow.exception;

public class FutureDateAllowedException extends RuntimeException {
    public FutureDateAllowedException(String message) {
        super(message);
    }
}
