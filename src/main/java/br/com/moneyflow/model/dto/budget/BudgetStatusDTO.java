package br.com.moneyflow.model.dto.budget;

import java.math.BigDecimal;

public record BudgetStatusDTO(
        Long id,
        Long categoryId,
        String categoryName,
        Integer month,
        Integer year,
        BigDecimal budgetAmount,
        BigDecimal spentAmount,
        BigDecimal remainingAmount,
        BigDecimal percentageUsed,
        BudgetStatus status,
        Long transactionCount
) {
    public enum BudgetStatus {
        WITHIN_BUDGET,      // < 80%
        NEAR_LIMIT,         // >= 80% e < 100%
        EXCEEDED            // >= 100%
    }

    public static BudgetStatus calculateStatus(BigDecimal percentageUsed) {
        if (percentageUsed.compareTo(BigDecimal.valueOf(100)) >= 0) {
            return BudgetStatus.EXCEEDED;
        } else if (percentageUsed.compareTo(BigDecimal.valueOf(80)) >= 0) {
            return BudgetStatus.NEAR_LIMIT;
        }
        return BudgetStatus.WITHIN_BUDGET;
    }
}