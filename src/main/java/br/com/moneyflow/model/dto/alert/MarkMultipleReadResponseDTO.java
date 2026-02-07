package br.com.moneyflow.model.dto.alert;

public record MarkMultipleReadResponseDTO(
        Integer updatedCount,
        String message
) {
}
