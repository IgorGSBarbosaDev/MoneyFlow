package br.com.moneyflow.model.dto.dashboard;

import br.com.moneyflow.model.dto.budget.BudgetStatusDTO;
import br.com.moneyflow.model.dto.transaction.CategoryExpenseDTO;
import br.com.moneyflow.model.dto.transaction.TransactionResponseDTO;

import java.math.BigDecimal;
import java.util.List;

public record MonthlySummaryDTO(
        Integer month,
        Integer year,
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal balance,
        BigDecimal savingsRate,
        List<CategoryExpenseDTO> expensesByCategory,
        List<BudgetStatusDTO> budgetStatus,
        Long activeAlertsCount,
        List<TransactionResponseDTO> recentTransactions
) {
}
