package br.com.moneyflow.exception.business;

import br.com.moneyflow.exception.base.BusinessRuleException;
import org.springframework.http.HttpStatus;

public class InvalidPasswordException extends BusinessRuleException {
    private static final String ERROR_CODE = "USER_003";

    public InvalidPasswordException(String message) {
        super(message, ERROR_CODE, HttpStatus.BAD_REQUEST);
    }
}
