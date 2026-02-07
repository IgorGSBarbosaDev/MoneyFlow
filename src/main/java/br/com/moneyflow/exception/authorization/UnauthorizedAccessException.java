package br.com.moneyflow.exception.authorization;

import br.com.moneyflow.exception.base.AuthorizationException;

public class UnauthorizedAccessException extends AuthorizationException {
    private static final String ERROR_CODE = "AUTH_001";

    public UnauthorizedAccessException(String message) {
        super(message, ERROR_CODE);
    }
}
