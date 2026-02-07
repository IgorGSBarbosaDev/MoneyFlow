package br.com.moneyflow.exception.business;

import br.com.moneyflow.exception.base.BusinessRuleException;
import org.springframework.http.HttpStatus;

public class InvalidMonthException extends BusinessRuleException {
    private static final String ERROR_CODE = "BUDGET_004";

    public InvalidMonthException(String message) {
        super(message, ERROR_CODE, HttpStatus.BAD_REQUEST);
    }
}
