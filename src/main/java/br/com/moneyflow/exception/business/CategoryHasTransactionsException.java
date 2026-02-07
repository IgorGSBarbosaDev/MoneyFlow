package br.com.moneyflow.exception.business;

import br.com.moneyflow.exception.base.BusinessRuleException;
import org.springframework.http.HttpStatus;

public class CategoryHasTransactionsException extends BusinessRuleException {
    private static final String ERROR_CODE = "CATEGORY_003";

    public CategoryHasTransactionsException(String message) {
        super(message, ERROR_CODE, HttpStatus.CONFLICT);
    }
}
