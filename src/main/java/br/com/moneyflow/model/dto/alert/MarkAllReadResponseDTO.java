package br.com.moneyflow.model.dto.alert;

public record MarkAllReadResponseDTO(
        Integer updatedCount,
        String message
) {
}
