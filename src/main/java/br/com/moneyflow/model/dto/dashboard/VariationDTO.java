package br.com.moneyflow.model.dto.dashboard;

import java.math.BigDecimal;

public record VariationDTO(
        BigDecimal absolute,
        BigDecimal percentage
) {
}
