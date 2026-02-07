package br.com.moneyflow.model.dto.alert;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record MarkMultipleReadRequestDTO(
        @NotEmpty(message = "A lista de IDs não pode estar vazia")
        List<Long> alertIds
) {
}
