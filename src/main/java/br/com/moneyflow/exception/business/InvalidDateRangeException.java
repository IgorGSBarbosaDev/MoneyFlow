package br.com.moneyflow.exception.business;

import br.com.moneyflow.exception.base.BusinessRuleException;
import org.springframework.http.HttpStatus;

public class InvalidDateRangeException extends BusinessRuleException {
    private static final String ERROR_CODE = "TRANSACTION_006";

    public InvalidDateRangeException(String message) {
        super(message, ERROR_CODE, HttpStatus.BAD_REQUEST);
    }
}
