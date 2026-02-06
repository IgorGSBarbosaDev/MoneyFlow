package br.com.moneyflow.service;

import br.com.moneyflow.exception.business.InvalidMonthException;
import br.com.moneyflow.exception.business.InvalidYearException;
import br.com.moneyflow.model.dto.budget.BudgetStatusDTO;
import br.com.moneyflow.model.dto.dashboard.*;
import br.com.moneyflow.model.dto.transaction.CategoryExpenseDTO;
import br.com.moneyflow.model.dto.transaction.TransactionResponseDTO;
import br.com.moneyflow.model.entity.Transaction;
import br.com.moneyflow.repository.AlertRepository;
import br.com.moneyflow.repository.BudgetRepository;
import br.com.moneyflow.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;
    private final AlertRepository alertRepository;

    @Transactional(readOnly = true)
    public MonthlySummaryDTO getMonthlySummary(Long userId, Integer month, Integer year) {
        validateMonth(month);
        validateYear(year);

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        BigDecimal totalIncome = transactionRepository.sumIncomeByPeriod(userId, startDate, endDate);
        BigDecimal totalExpense = transactionRepository.sumExpensesByPeriod(userId, startDate, endDate);

        if (totalIncome == null) totalIncome = BigDecimal.ZERO;
        if (totalExpense == null) totalExpense = BigDecimal.ZERO;

        BigDecimal balance = totalIncome.subtract(totalExpense);
        BigDecimal savingsRate = calculateSavingsRate(totalIncome, totalExpense);

        List<CategoryExpenseDTO> expensesByCategory = getExpensesByCategory(userId, startDate, endDate, totalExpense);

        List<BudgetStatusDTO> budgetStatus = getBudgetStatus(userId, month, year);

        Long activeAlertsCount = alertRepository.countByUserIdAndReadFalse(userId);

        List<TransactionResponseDTO> recentTransactions = getRecentTransactions(userId, 5);

        return new MonthlySummaryDTO(
                month,
                year,
                totalIncome,
                totalExpense,
                balance,
                savingsRate,
                expensesByCategory,
                budgetStatus,
                activeAlertsCount,
                recentTransactions
        );
    }

    @Transactional(readOnly = true)
    public MonthlyComparisonDTO getMonthlyComparison(Long userId, Integer month, Integer year) {
        validateMonth(month);
        validateYear(year);

        YearMonth currentYearMonth = YearMonth.of(year, month);
        YearMonth previousYearMonth = currentYearMonth.minusMonths(1);

        MonthDataDTO currentMonthData = getMonthData(userId, currentYearMonth);

        MonthDataDTO previousMonthData = getMonthData(userId, previousYearMonth);

        VariationDTO incomeVariation = calculateVariation(
                previousMonthData.income(), currentMonthData.income());
        VariationDTO expenseVariation = calculateVariation(
                previousMonthData.expense(), currentMonthData.expense());
        VariationDTO balanceVariation = calculateVariation(
                previousMonthData.balance(), currentMonthData.balance());

        List<CategoryVariationDTO> categoryVariations = getCategoryVariations(
                userId, previousYearMonth, currentYearMonth);

        List<CategoryVariationDTO> biggestIncreases = categoryVariations.stream()
                .filter(cv -> cv.absoluteVariation().compareTo(BigDecimal.ZERO) > 0)
                .sorted(Comparator.comparing(CategoryVariationDTO::absoluteVariation).reversed())
                .limit(5)
                .collect(Collectors.toList());

        List<CategoryVariationDTO> biggestDecreases = categoryVariations.stream()
                .filter(cv -> cv.absoluteVariation().compareTo(BigDecimal.ZERO) < 0)
                .sorted(Comparator.comparing(CategoryVariationDTO::absoluteVariation))
                .limit(5)
                .collect(Collectors.toList());

        return new MonthlyComparisonDTO(
                currentMonthData,
                previousMonthData,
                incomeVariation,
                expenseVariation,
                balanceVariation,
                biggestIncreases,
                biggestDecreases
        );
    }

    @Transactional(readOnly = true)
    public YearlyOverviewDTO getYearlyOverview(Long userId, Integer year) {
        validateYear(year);

        List<MonthDataDTO> monthlyData = new ArrayList<>();
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;
        int monthsWithData = 0;

        for (int month = 1; month <= 12; month++) {
            YearMonth yearMonth = YearMonth.of(year, month);

            if (yearMonth.isAfter(YearMonth.now())) {
                break;
            }

            MonthDataDTO monthData = getMonthData(userId, yearMonth);
            monthlyData.add(monthData);

            if (monthData.income().compareTo(BigDecimal.ZERO) > 0 ||
                monthData.expense().compareTo(BigDecimal.ZERO) > 0) {
                totalIncome = totalIncome.add(monthData.income());
                totalExpense = totalExpense.add(monthData.expense());
                monthsWithData++;
            }
        }

        BigDecimal totalBalance = totalIncome.subtract(totalExpense);

        BigDecimal avgIncome = monthsWithData > 0
                ? totalIncome.divide(BigDecimal.valueOf(monthsWithData), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        BigDecimal avgExpense = monthsWithData > 0
                ? totalExpense.divide(BigDecimal.valueOf(monthsWithData), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        BigDecimal avgBalance = monthsWithData > 0
                ? totalBalance.divide(BigDecimal.valueOf(monthsWithData), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        MonthDataDTO bestMonth = findBestMonth(monthlyData);
        MonthDataDTO worstMonth = findWorstMonth(monthlyData);

        BigDecimal savingsRate = calculateSavingsRate(totalIncome, totalExpense);

        TrendDTO incomeTrend = calculateTrend(monthlyData, true);
        TrendDTO expenseTrend = calculateTrend(monthlyData, false);

        return new YearlyOverviewDTO(
                year,
                totalIncome,
                totalExpense,
                totalBalance,
                avgIncome,
                avgExpense,
                avgBalance,
                bestMonth,
                worstMonth,
                savingsRate,
                monthlyData,
                incomeTrend,
                expenseTrend
        );
    }

    private void validateMonth(Integer month) {
        if (month == null || month < 1 || month > 12) {
            throw new InvalidMonthException("Mês deve estar entre 1 e 12");
        }
    }

    private void validateYear(Integer year) {
        int currentYear = Year.now().getValue();
        if (year == null || year < 2000 || year > currentYear + 1) {
            throw new InvalidYearException(
                    String.format("Ano deve estar entre 2000 e %d", currentYear + 1));
        }
    }

    private BigDecimal calculateSavingsRate(BigDecimal income, BigDecimal expense) {
        if (income == null || income.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal savings = income.subtract(expense);
        return savings
                .multiply(BigDecimal.valueOf(100))
                .divide(income, 2, RoundingMode.HALF_UP);
    }

    private List<CategoryExpenseDTO> getExpensesByCategory(Long userId, LocalDate startDate,
                                                            LocalDate endDate, BigDecimal totalExpense) {
        var projections = transactionRepository.findExpensesByCategory(userId, startDate, endDate);

        return projections.stream()
                .map(p -> {
                    BigDecimal percentage = BigDecimal.ZERO;
                    if (totalExpense.compareTo(BigDecimal.ZERO) > 0) {
                        percentage = p.getTotalAmount()
                                .multiply(BigDecimal.valueOf(100))
                                .divide(totalExpense, 2, RoundingMode.HALF_UP);
                    }
                    return new CategoryExpenseDTO(
                            p.getCategoryId(),
                            p.getCategoryName(),
                            p.getTotalAmount(),
                            percentage,
                            p.getTransactionCount().longValue()
                    );
                })
                .collect(Collectors.toList());
    }

    private List<BudgetStatusDTO> getBudgetStatus(Long userId, Integer month, Integer year) {
        var projections = budgetRepository.findBudgetStatusByUserAndPeriod(userId, month, year);

        return projections.stream()
                .map(p -> {
                    BigDecimal budgetAmount = p.getBudgetAmount();
                    BigDecimal spentAmount = p.getSpentAmount() != null ? p.getSpentAmount() : BigDecimal.ZERO;
                    BigDecimal remaining = p.getRemainingAmount();
                    BigDecimal percentageUsed = p.getPercentageUsed();
                    BudgetStatusDTO.BudgetStatus status = BudgetStatusDTO.calculateStatus(percentageUsed);

                    return new BudgetStatusDTO(
                            p.getId(),
                            p.getCategoryId(),
                            p.getCategoryName(),
                            month,
                            year,
                            budgetAmount,
                            spentAmount,
                            remaining,
                            percentageUsed,
                            status,
                            0L
                    );
                })
                .sorted(Comparator.comparing(BudgetStatusDTO::percentageUsed).reversed())
                .collect(Collectors.toList());
    }

    private List<TransactionResponseDTO> getRecentTransactions(Long userId, int limit) {
        List<Transaction> transactions = transactionRepository.findRecentTransactions(
                userId, PageRequest.of(0, limit));

        return transactions.stream()
                .map(this::toTransactionResponseDTO)
                .collect(Collectors.toList());
    }

    private TransactionResponseDTO toTransactionResponseDTO(Transaction t) {
        return new TransactionResponseDTO(
                t.getId(),
                t.getDescription(),
                t.getAmount(),
                t.getDate(),
                t.getType(),
                t.getCategory().getId(),
                t.getCategory().getName(),
                t.getPaymentMethod(),
                t.getNotes(),
                t.getCreatedAt(),
                t.getUpdatedAt()
        );
    }

    private MonthDataDTO getMonthData(Long userId, YearMonth yearMonth) {
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        BigDecimal income = transactionRepository.sumIncomeByPeriod(userId, startDate, endDate);
        BigDecimal expense = transactionRepository.sumExpensesByPeriod(userId, startDate, endDate);

        if (income == null) income = BigDecimal.ZERO;
        if (expense == null) expense = BigDecimal.ZERO;

        BigDecimal balance = income.subtract(expense);

        return new MonthDataDTO(
                yearMonth.getMonthValue(),
                yearMonth.getYear(),
                income,
                expense,
                balance
        );
    }

    private VariationDTO calculateVariation(BigDecimal previous, BigDecimal current) {
        BigDecimal absolute = current.subtract(previous);
        BigDecimal percentage = BigDecimal.ZERO;

        if (previous.compareTo(BigDecimal.ZERO) != 0) {
            percentage = absolute
                    .multiply(BigDecimal.valueOf(100))
                    .divide(previous, 2, RoundingMode.HALF_UP);
        } else if (current.compareTo(BigDecimal.ZERO) > 0) {
            percentage = BigDecimal.valueOf(100); // De zero para algo = 100% de aumento
        }

        return new VariationDTO(absolute, percentage);
    }

    private List<CategoryVariationDTO> getCategoryVariations(Long userId,
                                                              YearMonth previousMonth,
                                                              YearMonth currentMonth) {
        Map<Long, TransactionRepository.CategoryExpenseProjection> previousExpenses =
                transactionRepository.findExpensesByCategoryAndMonth(
                        userId, previousMonth.getYear(), previousMonth.getMonthValue())
                .stream()
                .collect(Collectors.toMap(
                        TransactionRepository.CategoryExpenseProjection::getCategoryId,
                        p -> p
                ));

        Map<Long, TransactionRepository.CategoryExpenseProjection> currentExpenses =
                transactionRepository.findExpensesByCategoryAndMonth(
                        userId, currentMonth.getYear(), currentMonth.getMonthValue())
                .stream()
                .collect(Collectors.toMap(
                        TransactionRepository.CategoryExpenseProjection::getCategoryId,
                        p -> p
                ));

        Set<Long> allCategoryIds = new HashSet<>();
        allCategoryIds.addAll(previousExpenses.keySet());
        allCategoryIds.addAll(currentExpenses.keySet());

        List<CategoryVariationDTO> variations = new ArrayList<>();

        for (Long categoryId : allCategoryIds) {
            var prev = previousExpenses.get(categoryId);
            var curr = currentExpenses.get(categoryId);

            BigDecimal previousAmount = prev != null ? prev.getTotalAmount() : BigDecimal.ZERO;
            BigDecimal currentAmount = curr != null ? curr.getTotalAmount() : BigDecimal.ZERO;
            String categoryName = curr != null ? curr.getCategoryName() :
                    (prev != null ? prev.getCategoryName() : "Unknown");

            BigDecimal absoluteVariation = currentAmount.subtract(previousAmount);
            BigDecimal percentageVariation = BigDecimal.ZERO;

            if (previousAmount.compareTo(BigDecimal.ZERO) != 0) {
                percentageVariation = absoluteVariation
                        .multiply(BigDecimal.valueOf(100))
                        .divide(previousAmount, 2, RoundingMode.HALF_UP);
            } else if (currentAmount.compareTo(BigDecimal.ZERO) > 0) {
                percentageVariation = BigDecimal.valueOf(100);
            }

            variations.add(new CategoryVariationDTO(
                    categoryId,
                    categoryName,
                    previousAmount,
                    currentAmount,
                    absoluteVariation,
                    percentageVariation
            ));
        }

        return variations;
    }

    private MonthDataDTO findBestMonth(List<MonthDataDTO> monthlyData) {
        return monthlyData.stream()
                .filter(m -> m.income().compareTo(BigDecimal.ZERO) > 0 ||
                             m.expense().compareTo(BigDecimal.ZERO) > 0)
                .max(Comparator.comparing(MonthDataDTO::balance))
                .orElse(null);
    }

    private MonthDataDTO findWorstMonth(List<MonthDataDTO> monthlyData) {
        return monthlyData.stream()
                .filter(m -> m.income().compareTo(BigDecimal.ZERO) > 0 ||
                             m.expense().compareTo(BigDecimal.ZERO) > 0)
                .min(Comparator.comparing(MonthDataDTO::balance))
                .orElse(null);
    }

    private TrendDTO calculateTrend(List<MonthDataDTO> monthlyData, boolean isIncome) {
        List<MonthDataDTO> dataWithMovement = monthlyData.stream()
                .filter(m -> m.income().compareTo(BigDecimal.ZERO) > 0 ||
                             m.expense().compareTo(BigDecimal.ZERO) > 0)
                .collect(Collectors.toList());

        if (dataWithMovement.size() < 2) {
            return new TrendDTO(
                    YearlyOverviewDTO.TrendDirection.STABLE,
                    BigDecimal.ZERO,
                    "Dados insuficientes para calcular tendência"
            );
        }

        int midPoint = dataWithMovement.size() / 2;

        BigDecimal firstHalfAvg = BigDecimal.ZERO;
        BigDecimal secondHalfAvg = BigDecimal.ZERO;

        for (int i = 0; i < midPoint; i++) {
            MonthDataDTO m = dataWithMovement.get(i);
            firstHalfAvg = firstHalfAvg.add(isIncome ? m.income() : m.expense());
        }

        for (int i = midPoint; i < dataWithMovement.size(); i++) {
            MonthDataDTO m = dataWithMovement.get(i);
            secondHalfAvg = secondHalfAvg.add(isIncome ? m.income() : m.expense());
        }

        if (midPoint > 0) {
            firstHalfAvg = firstHalfAvg.divide(BigDecimal.valueOf(midPoint), 2, RoundingMode.HALF_UP);
        }

        int secondHalfCount = dataWithMovement.size() - midPoint;
        if (secondHalfCount > 0) {
            secondHalfAvg = secondHalfAvg.divide(BigDecimal.valueOf(secondHalfCount), 2, RoundingMode.HALF_UP);
        }

        BigDecimal change = secondHalfAvg.subtract(firstHalfAvg);
        BigDecimal percentageChange = BigDecimal.ZERO;

        if (firstHalfAvg.compareTo(BigDecimal.ZERO) != 0) {
            percentageChange = change
                    .multiply(BigDecimal.valueOf(100))
                    .divide(firstHalfAvg, 2, RoundingMode.HALF_UP);
        }

        YearlyOverviewDTO.TrendDirection direction;
        String description;
        String type = isIncome ? "receitas" : "despesas";

        if (percentageChange.compareTo(BigDecimal.valueOf(5)) > 0) {
            direction = YearlyOverviewDTO.TrendDirection.INCREASING;
            description = String.format("As %s estão aumentando (%.1f%%)", type, percentageChange);
        } else if (percentageChange.compareTo(BigDecimal.valueOf(-5)) < 0) {
            direction = YearlyOverviewDTO.TrendDirection.DECREASING;
            description = String.format("As %s estão diminuindo (%.1f%%)", type, percentageChange.abs());
        } else {
            direction = YearlyOverviewDTO.TrendDirection.STABLE;
            description = String.format("As %s estão estáveis", type);
        }

        return new TrendDTO(direction, percentageChange, description);
    }
}

