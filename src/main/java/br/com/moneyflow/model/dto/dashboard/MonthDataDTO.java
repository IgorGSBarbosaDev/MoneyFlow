package br.com.moneyflow.model.dto.dashboard;

import java.math.BigDecimal;

public record MonthDataDTO(
        Integer month,
        Integer year,
        BigDecimal income,
        BigDecimal expense,
        BigDecimal balance
) {
}
