package br.com.moneyflow.exception.resource;

import br.com.moneyflow.exception.base.ResourceNotFoundException;

public class TransactionNotFoundException extends ResourceNotFoundException {
    private static final String ERROR_CODE = "TRANSACTION_002";

    public TransactionNotFoundException(String message) {
        super(message, ERROR_CODE);
    }
}
