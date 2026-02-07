package br.com.moneyflow.exception.business;

import br.com.moneyflow.exception.base.BusinessRuleException;
import org.springframework.http.HttpStatus;

public class BudgetAlreadyExistsException extends BusinessRuleException {
    private static final String ERROR_CODE = "BUDGET_002";

    public BudgetAlreadyExistsException(String message) {
        super(message, ERROR_CODE, HttpStatus.CONFLICT);
    }
}
