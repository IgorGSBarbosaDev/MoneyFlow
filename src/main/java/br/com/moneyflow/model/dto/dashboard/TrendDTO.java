package br.com.moneyflow.model.dto.dashboard;

import java.math.BigDecimal;

public record TrendDTO(
        YearlyOverviewDTO.TrendDirection direction,
        BigDecimal percentageChange,
        String description
) {
}
