package br.com.moneyflow.exception.business;

import br.com.moneyflow.exception.base.BusinessRuleException;
import org.springframework.http.HttpStatus;

public class InvalidAmountException extends BusinessRuleException {
    private static final String ERROR_CODE = "TRANSACTION_004";

    public InvalidAmountException(String message) {
        super(message, ERROR_CODE, HttpStatus.BAD_REQUEST);
    }
}
