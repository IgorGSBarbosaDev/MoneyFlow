package br.com.moneyflow.exception.authorization;

@Deprecated
public class UnauthorizedAcessException extends RuntimeException {
    public UnauthorizedAcessException(String message) {
        super(message);
    }
}
