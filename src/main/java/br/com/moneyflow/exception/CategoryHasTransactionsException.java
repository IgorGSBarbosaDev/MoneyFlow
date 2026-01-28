package br.com.moneyflow.exception;

public class CategoryHasTransactionsException extends RuntimeException {
    public CategoryHasTransactionsException(String message) {
        super(message);
    }
}
