package br.com.moneyflow.exception.business;

import br.com.moneyflow.exception.base.BusinessRuleException;
import org.springframework.http.HttpStatus;

public class DuplicateEmailException extends BusinessRuleException {
    private static final String ERROR_CODE = "USER_002";

    public DuplicateEmailException(String message) {
        super(message, ERROR_CODE, HttpStatus.CONFLICT);
    }
}
