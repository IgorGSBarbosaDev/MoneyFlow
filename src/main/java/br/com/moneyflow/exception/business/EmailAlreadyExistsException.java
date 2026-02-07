package br.com.moneyflow.exception.business;

import br.com.moneyflow.exception.base.BusinessRuleException;
import br.com.moneyflow.exception.base.BusinessRuleException;
import org.springframework.http.HttpStatus;

public class EmailAlreadyExistsException extends BusinessRuleException {
    private static final String ERROR_CODE = "USER_002";

    public EmailAlreadyExistsException(String message) {
        super(message, ERROR_CODE, HttpStatus.CONFLICT);
    }
}
