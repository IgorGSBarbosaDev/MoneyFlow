package br.com.moneyflow.model.dto.error;

public record FieldErrorDTO(
        String field,
        String message
) {
}
