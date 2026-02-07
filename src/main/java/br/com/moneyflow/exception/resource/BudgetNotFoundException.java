package br.com.moneyflow.exception.resource;

import br.com.moneyflow.exception.base.ResourceNotFoundException;

public class BudgetNotFoundException extends ResourceNotFoundException {
    private static final String ERROR_CODE = "BUDGET_001";

    public BudgetNotFoundException(String message) {
        super(message, ERROR_CODE);
    }
}
