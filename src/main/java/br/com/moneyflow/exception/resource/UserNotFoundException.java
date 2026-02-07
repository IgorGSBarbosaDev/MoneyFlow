package br.com.moneyflow.exception.resource;

import br.com.moneyflow.exception.base.ResourceNotFoundException;

public class UserNotFoundException extends ResourceNotFoundException {

    private static final String ERROR_CODE = "USER_001";

    public UserNotFoundException(String message) {
        super(message, ERROR_CODE);
    }

    public UserNotFoundException(Long userId) {
        super("Usuário não encontrado com ID: " + userId, ERROR_CODE);
    }
}
