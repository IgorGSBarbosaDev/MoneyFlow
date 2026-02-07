package br.com.moneyflow.exception.authorization;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException(String message) {
        super(message);
    }

    public InvalidCredentialsException() {
        super("Email ou senha inválidos");
    }
}
