package br.com.moneyflow.model.dto.transaction;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionSummaryDTO(
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal balance,
        Long transactionCount) {
}
