package br.com.moneyflow.exception.business;

import br.com.moneyflow.exception.base.BusinessRuleException;
import org.springframework.http.HttpStatus;

public class CategoryTypeChangeNotAllowedException extends BusinessRuleException {
    private static final String ERROR_CODE = "CATEGORY_004";

    public CategoryTypeChangeNotAllowedException(String message) {
        super(message, ERROR_CODE, HttpStatus.BAD_REQUEST);
    }
}
