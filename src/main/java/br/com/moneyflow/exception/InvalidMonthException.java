package br.com.moneyflow.exception;

public class InvalidMonthException extends RuntimeException {
    public InvalidMonthException(String message) {
        super(message);
    }
}
