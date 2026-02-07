package br.com.moneyflow.exception.business;

public class InactiveUserException extends RuntimeException {
    public InactiveUserException(String message) {
        super(message);
    }

    public InactiveUserException() {
        super("Usuário está inativo");
    }
}
