package br.com.moneyflow.exception.resource;

import br.com.moneyflow.exception.base.ResourceNotFoundException;

public class CategoryNotFoundException extends ResourceNotFoundException {
    private static final String ERROR_CODE = "CATEGORY_001";

    public CategoryNotFoundException(String message) {
        super(message, ERROR_CODE);
    }
}
