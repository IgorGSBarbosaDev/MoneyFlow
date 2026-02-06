package br.com.moneyflow.model.dto.dashboard;

import java.math.BigDecimal;

public record CategoryVariationDTO(
        Long categoryId,
        String categoryName,
        BigDecimal previousAmount,
        BigDecimal currentAmount,
        BigDecimal absoluteVariation,
        BigDecimal percentageVariation
) {
}
