package br.com.moneyflow.exception.resource;

import br.com.moneyflow.exception.base.ResourceNotFoundException;

public class AlertNotFoundException extends ResourceNotFoundException {
    private static final String ERROR_CODE = "ALERT_001";

    public AlertNotFoundException(String message) {
        super(message, ERROR_CODE);
    }
}
