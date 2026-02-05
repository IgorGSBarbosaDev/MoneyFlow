package br.com.moneyflow.service;

import br.com.moneyflow.exception.*;
import br.com.moneyflow.model.dto.budget.BudgetRequestDTO;
import br.com.moneyflow.model.dto.budget.BudgetResponseDTO;
import br.com.moneyflow.model.dto.budget.BudgetStatusDTO;
import br.com.moneyflow.model.dto.budget.BudgetUpdateDTO;
import br.com.moneyflow.model.dto.projection.BudgetStatusProjection;
import br.com.moneyflow.model.entity.Budget;
import br.com.moneyflow.model.entity.Category;
import br.com.moneyflow.model.entity.CategoryType;
import br.com.moneyflow.model.entity.User;
import br.com.moneyflow.repository.BudgetRepository;
import br.com.moneyflow.repository.CategoryRepository;
import br.com.moneyflow.repository.TransactionRepository;
import br.com.moneyflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Year;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final AlertService alertService;

    @Transactional
    public BudgetResponseDTO createBudget(Long userId, BudgetRequestDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado com id: " + userId));

        Category category = categoryRepository.findByUserIdAndId(userId, dto.categoryId())
                .orElseThrow(() -> new CategoryNotFoundException(
                        "Categoria não encontrada ou não pertence ao usuário: " + dto.categoryId()));

        if (category.getType() != CategoryType.EXPENSE) {
            throw new InvalidCategoryTypeException(
                    "Apenas categorias de despesa (EXPENSE) podem ter orçamento definido");
        }

        validateMonth(dto.month());

        validateYear(dto.year());

        if (dto.amount() == null || dto.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("O valor do orçamento deve ser maior que zero");
        }

        if (budgetRepository.existsByUserIdAndCategoryIdAndMonthAndYear(
                userId, dto.categoryId(), dto.month(), dto.year())) {
            throw new BudgetAlreadyExistsException(
                    String.format("Já existe um orçamento para esta categoria no período %02d/%d",
                            dto.month(), dto.year()));
        }

        Budget budget = Budget.builder()
                .user(user)
                .category(category)
                .amount(dto.amount())
                .month(dto.month())
                .year(dto.year())
                .build();

        Budget savedBudget = budgetRepository.save(budget);

        BigDecimal currentSpent = transactionRepository.sumExpensesByCategoryAndMonth(
                userId, category.getId(), dto.year(), dto.month());

        if (currentSpent == null) {
            currentSpent = BigDecimal.ZERO;
        }

        alertService.checkAndSendBudgetAlert(savedBudget, currentSpent);

        return toBudgetResponseDTO(savedBudget);
    }


    public BudgetStatusDTO getBudgetById(Long userId, Long budgetId) {
        Budget budget = budgetRepository.findByIdAndUserId(budgetId, userId)
                .orElseThrow(() -> new BudgetNotFoundException(
                        "Orçamento não encontrado com id: " + budgetId));

        BigDecimal spentAmount = transactionRepository.sumExpensesByCategoryAndMonth(
                userId,
                budget.getCategory().getId(),
                budget.getYear(),
                budget.getMonth());

        if (spentAmount == null) {
            spentAmount = BigDecimal.ZERO;
        }

        Long transactionCount = countTransactionsForBudget(userId, budget);

        return toBudgetStatusDTO(budget, spentAmount, transactionCount);
    }

    public List<BudgetStatusDTO> getBudgetsByUserAndPeriod(Long userId, Integer month, Integer year) {
        validateMonth(month);
        validateYear(year);

        List<BudgetStatusProjection> projections = budgetRepository.findBudgetStatusByUserAndPeriod(
                userId, month, year);

        return projections.stream()
                .map(p -> toBudgetStatusDTOFromProjection(p, month, year))
                .sorted(Comparator.comparing(BudgetStatusDTO::percentageUsed).reversed())
                .collect(Collectors.toList());
    }

    @Transactional
    public BudgetResponseDTO updateBudget(Long userId, Long budgetId, BudgetUpdateDTO dto) {
        Budget budget = budgetRepository.findByIdAndUserId(budgetId, userId)
                .orElseThrow(() -> new BudgetNotFoundException(
                        "Orçamento não encontrado com id: " + budgetId));

        if (dto.amount() == null || dto.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("O valor do orçamento deve ser maior que zero");
        }

        budget.setAmount(dto.amount());

        Budget updatedBudget = budgetRepository.save(budget);

        BigDecimal currentSpent = transactionRepository.sumExpensesByCategoryAndMonth(
                userId,
                budget.getCategory().getId(),
                budget.getYear(),
                budget.getMonth());

        if (currentSpent == null) {
            currentSpent = BigDecimal.ZERO;
        }

        alertService.checkAndSendBudgetAlert(updatedBudget, currentSpent);

        return toBudgetResponseDTO(updatedBudget);
    }

    @Transactional
    public void deleteBudget(Long userId, Long budgetId) {
        Budget budget = budgetRepository.findByIdAndUserId(budgetId, userId)
                .orElseThrow(() -> new BudgetNotFoundException(
                        "Orçamento não encontrado com id: " + budgetId));

        budgetRepository.delete(budget);
    }

    @Transactional
    public int checkBudgetsAndGenerateAlerts(Long userId, Integer month, Integer year) {
        validateMonth(month);
        validateYear(year);

        List<Budget> budgets = budgetRepository.findByUserIdAndMonthAndYear(userId, month, year);
        int alertsCreated = 0;

        for (Budget budget : budgets) {
            BigDecimal spent = transactionRepository.sumExpensesByCategoryAndMonth(
                    userId,
                    budget.getCategory().getId(),
                    year,
                    month);

            if (spent == null) {
                spent = BigDecimal.ZERO;
            }

            alertService.checkAndSendBudgetAlert(budget, spent);

            BigDecimal percentage = calculatePercentage(spent, budget.getAmount());
            if (percentage.compareTo(BigDecimal.valueOf(80)) >= 0) {
                alertsCreated++;
            }
        }

        return alertsCreated;
    }

    public List<BudgetResponseDTO> getAllBudgetsByUser(Long userId) {
        List<Budget> budgets = budgetRepository.findByUserId(userId);
        return budgets.stream()
                .map(this::toBudgetResponseDTO)
                .collect(Collectors.toList());
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

    private BigDecimal calculatePercentage(BigDecimal spent, BigDecimal budget) {
        if (budget == null || budget.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return spent
                .multiply(BigDecimal.valueOf(100))
                .divide(budget, 2, RoundingMode.HALF_UP);
    }

    private BudgetStatusDTO.BudgetStatus determineStatus(BigDecimal percentageUsed) {
        return BudgetStatusDTO.calculateStatus(percentageUsed);
    }

    private Long countTransactionsForBudget(Long userId, Budget budget) {
        LocalDate startDate = LocalDate.of(budget.getYear(), budget.getMonth(), 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        return transactionRepository.countByFilters(
                userId,
                budget.getCategory().getId(),
                null,
                startDate,
                endDate);
    }

    private BudgetResponseDTO toBudgetResponseDTO(Budget budget) {
        return new BudgetResponseDTO(
                budget.getId(),
                budget.getCategory().getId(),
                budget.getCategory().getName(),
                budget.getAmount(),
                budget.getMonth(),
                budget.getYear(),
                budget.getCreatedAt(),
                budget.getUpdatedAt()
        );
    }

    private BudgetStatusDTO toBudgetStatusDTO(Budget budget, BigDecimal spentAmount, Long transactionCount) {
        BigDecimal budgetAmount = budget.getAmount();
        BigDecimal remaining = budgetAmount.subtract(spentAmount);
        BigDecimal percentageUsed = calculatePercentage(spentAmount, budgetAmount);
        BudgetStatusDTO.BudgetStatus status = determineStatus(percentageUsed);

        return new BudgetStatusDTO(
                budget.getId(),
                budget.getCategory().getId(),
                budget.getCategory().getName(),
                budget.getMonth(),
                budget.getYear(),
                budgetAmount,
                spentAmount,
                remaining,
                percentageUsed,
                status,
                transactionCount
        );
    }

    private BudgetStatusDTO toBudgetStatusDTOFromProjection(BudgetStatusProjection projection,
                                                             Integer month, Integer year) {
        BigDecimal budgetAmount = projection.getBudgetAmount();
        BigDecimal spentAmount = projection.getSpentAmount() != null ?
                projection.getSpentAmount() : BigDecimal.ZERO;
        BigDecimal remaining = projection.getRemainingAmount();
        BigDecimal percentageUsed = projection.getPercentageUsed();
        BudgetStatusDTO.BudgetStatus status = determineStatus(percentageUsed);

        return new BudgetStatusDTO(
                projection.getId(),
                projection.getCategoryId(),
                projection.getCategoryName(),
                month,
                year,
                budgetAmount,
                spentAmount,
                remaining,
                percentageUsed,
                status,
                0L
        );
    }
}
