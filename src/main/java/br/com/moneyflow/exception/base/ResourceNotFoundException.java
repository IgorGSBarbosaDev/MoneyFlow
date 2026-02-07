package br.com.moneyflow.exception.base;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BaseException {

    public ResourceNotFoundException(String message, String errorCode) {
        super(message, errorCode, HttpStatus.NOT_FOUND);
    }
}
