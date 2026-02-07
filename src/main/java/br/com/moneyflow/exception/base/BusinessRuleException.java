package br.com.moneyflow.exception.base;

import org.springframework.http.HttpStatus;

public class BusinessRuleException extends BaseException {

    public BusinessRuleException(String message, String errorCode, HttpStatus httpStatus) {
        super(message, errorCode, httpStatus);
    }
}
