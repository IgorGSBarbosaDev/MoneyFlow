package br.com.moneyflow.model.dto.dashboard;

import java.math.BigDecimal;
import java.util.List;

public record YearlyOverviewDTO(
        Integer year,
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal totalBalance,
        BigDecimal averageMonthlyIncome,
        BigDecimal averageMonthlyExpense,
        BigDecimal averageMonthlyBalance,
        MonthDataDTO bestMonth,
        MonthDataDTO worstMonth,
        BigDecimal savingsRate,
        List<MonthDataDTO> monthlyData,
        TrendDTO incomeTrend,
        TrendDTO expenseTrend
) {
    public enum TrendDirection {
        INCREASING,
        DECREASING,
        STABLE
    }
}
