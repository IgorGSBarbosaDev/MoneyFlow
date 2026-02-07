package br.com.moneyflow.exception.authorization;

public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String message) {
        super(message);
    }

    public InvalidTokenException() {
        super("Token JWT inválido ou expirado");
    }
}
