package br.com.moneyflow.exception.business;

import br.com.moneyflow.exception.base.BusinessRuleException;
import org.springframework.http.HttpStatus;

public class DuplicateCategoryException extends BusinessRuleException {
    private static final String ERROR_CODE = "CATEGORY_002";

    public DuplicateCategoryException(String message) {
        super(message, ERROR_CODE, HttpStatus.CONFLICT);
    }
}
