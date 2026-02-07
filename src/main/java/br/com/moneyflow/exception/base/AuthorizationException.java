package br.com.moneyflow.exception.base;

import org.springframework.http.HttpStatus;

public class AuthorizationException extends BaseException {

    public AuthorizationException(String message, String errorCode) {
        super(message, errorCode, HttpStatus.FORBIDDEN);
    }
}
