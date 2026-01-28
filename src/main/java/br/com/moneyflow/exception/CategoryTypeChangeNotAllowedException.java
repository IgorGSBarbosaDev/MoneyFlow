package br.com.moneyflow.exception;

public class CategoryTypeChangeNotAllowedException extends RuntimeException {
    public CategoryTypeChangeNotAllowedException(String message) {
        super(message);
    }
}
