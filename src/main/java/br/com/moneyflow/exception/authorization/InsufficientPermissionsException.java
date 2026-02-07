package br.com.moneyflow.exception.authorization;

import br.com.moneyflow.exception.base.AuthorizationException;

public class InsufficientPermissionsException extends AuthorizationException {
    private static final String ERROR_CODE = "AUTH_002";

    public InsufficientPermissionsException(String message) {
        super(message, ERROR_CODE);
    }
}
