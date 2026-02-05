package br.com.moneyflow.service;

import br.com.moneyflow.exception.*;
import br.com.moneyflow.model.dto.transaction.CategoryExpenseDTO;
import br.com.moneyflow.model.dto.transaction.TransactionFilterDTO;
import br.com.moneyflow.model.dto.transaction.TransactionRequestDTO;
import br.com.moneyflow.model.dto.transaction.TransactionResponseDTO;
import br.com.moneyflow.model.entity.Budget;
import br.com.moneyflow.model.entity.Category;
import br.com.moneyflow.model.entity.Transaction;
import br.com.moneyflow.model.entity.TransactionType;
import br.com.moneyflow.repository.BudgetRepository;
import br.com.moneyflow.repository.CategoryRepository;
import br.com.moneyflow.repository.TransactionRepository;
import br.com.moneyflow.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final BudgetRepository budgetRepository;
    private final AlertService alertService;

    @Transactional
    public TransactionResponseDTO createTransaction(Long userId, TransactionRequestDTO transactionRequestDTO) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado com id: " + userId));

        var category = categoryRepository.findByUserIdAndCategoryId(userId, transactionRequestDTO.categoryId())
                .orElseThrow(() -> new CategoryNotFoundException("Categoria não encontrada com id: " + transactionRequestDTO.categoryId()));

        if (!transactionRequestDTO.type().name().equals(category.getType().name())) {
            throw new InvalidTransactionTypeException("Tipo da transação não corresponde ao tipo da categoria");
        }

        if (transactionRequestDTO.amount() == null || transactionRequestDTO.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Valor deve ser maior que zero");
        }

        if (transactionRequestDTO.description() == null || transactionRequestDTO.description().trim().length() < 3) {
            throw new ValidationException("Descrição deve ter no mínimo 3 caracteres");
        }

        if (transactionRequestDTO.date() != null && transactionRequestDTO.date().isAfter(LocalDate.now())) {
            throw new InvalidDateException("Data da transação não pode ser no futuro");
        }

        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setDescription(transactionRequestDTO.description());
        transaction.setAmount(transactionRequestDTO.amount());
        transaction.setDate(transactionRequestDTO.date());
        transaction.setType(transactionRequestDTO.type());
        transaction.setCategory(category);
        transaction.setPaymentMethod(transactionRequestDTO.paymentMethod());
        transaction.setNotes(transactionRequestDTO.notes());

        Transaction savedTransaction = transactionRepository.save(transaction);

        if (savedTransaction.getType() == TransactionType.EXPENSE) {
            checkBudgetAndCreateAlert(userId, category.getId(),
                    savedTransaction.getDate().getYear(),
                    savedTransaction.getDate().getMonthValue());
        }
        return toDTO(savedTransaction);
    }

    public TransactionResponseDTO getTransactionById(Long userId, Long transactionId) {
        Transaction transaction = transactionRepository.findByIdAndDeletedFalse(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException("Transação não encontrada com id: " + transactionId));

        validateTransactionOwnership(transaction, userId);

        return toDTO(transaction);
    }


    public Page<TransactionResponseDTO> getTransactions(Long userId, TransactionFilterDTO filters, Pageable pageable) {
        if (filters != null) {
            if (filters.startDate() != null && filters.endDate() != null
                    && filters.startDate().isAfter(filters.endDate())) {
                throw new InvalidDateRangeException("Data inicial não pode ser maior que data final");
            }

            if (filters.categoryId() != null) {
                categoryRepository.findByUserIdAndCategoryId(userId, filters.categoryId())
                        .orElseThrow(() -> new CategoryNotFoundException("Categoria não encontrada ou não pertence ao usuário"));
            }
        }

        LocalDate startDate = filters != null ? filters.startDate() : null;
        LocalDate endDate = filters != null ? filters.endDate() : null;
        Long categoryId = filters != null ? filters.categoryId() : null;
        TransactionType type = filters != null ? filters.type() : null;

        List<Transaction> transactions = transactionRepository.findByFilters(
                userId, categoryId, type, startDate, endDate, pageable);

        List<TransactionResponseDTO> dtos = transactions.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        long total = transactionRepository.countByFilters(userId, categoryId, type, startDate, endDate);

        return new PageImpl<>(dtos, pageable, total);
    }

    @Transactional
    public TransactionResponseDTO updateTransaction(Long userId, Long transactionId, TransactionRequestDTO dto) {
        Transaction transaction = transactionRepository.findByIdAndDeletedFalse(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException("Transação não encontrada com id: " + transactionId));

        validateTransactionOwnership(transaction, userId);

        Long oldCategoryId = transaction.getCategory().getId();
        LocalDate oldDate = transaction.getDate();
        TransactionType oldType = transaction.getType();

        Category newCategory = categoryRepository.findByUserIdAndCategoryId(userId, dto.categoryId())
                .orElseThrow(() -> new CategoryNotFoundException("Categoria não encontrada ou não pertence ao usuário"));

        if (!dto.type().name().equals(newCategory.getType().name())) {
            throw new InvalidTransactionTypeException("Tipo da transação não corresponde ao tipo da categoria");
        }

        if (dto.amount() == null || dto.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Valor deve ser maior que zero");
        }

        if (dto.description() == null || dto.description().trim().length() < 3) {
            throw new ValidationException("Descrição deve ter no mínimo 3 caracteres");
        }

        if (dto.date() != null && dto.date().isAfter(LocalDate.now())) {
            throw new InvalidDateException("Data da transação não pode ser no futuro");
        }

        transaction.setDescription(dto.description().trim());
        transaction.setAmount(dto.amount());
        transaction.setDate(dto.date());
        transaction.setType(dto.type());
        transaction.setCategory(newCategory);
        transaction.setPaymentMethod(dto.paymentMethod());
        transaction.setNotes(dto.notes());

        Transaction updatedTransaction = transactionRepository.save(transaction);

        if (dto.type() == TransactionType.EXPENSE || oldType == TransactionType.EXPENSE) {
            if (!oldCategoryId.equals(dto.categoryId())) {
                recalculateBudgetAlerts(userId, oldCategoryId, oldDate.getYear(), oldDate.getMonthValue());
            }

            LocalDate newDate = dto.date();
            if (newDate != null && (oldDate.getMonthValue() != newDate.getMonthValue()
                    || oldDate.getYear() != newDate.getYear())) {
                recalculateBudgetAlerts(userId, oldCategoryId, oldDate.getYear(), oldDate.getMonthValue());
            }

            if (dto.type() == TransactionType.EXPENSE && newDate != null) {
                recalculateBudgetAlerts(userId, dto.categoryId(), newDate.getYear(), newDate.getMonthValue());
            }
        }

        return toDTO(updatedTransaction);
    }

    @Transactional
    public void deleteTransaction(Long userId, Long transactionId) {
        Transaction transaction = transactionRepository.findByIdAndDeletedFalse(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException("Transação não encontrada com id: " + transactionId));

        validateTransactionOwnership(transaction, userId);

        Long categoryId = transaction.getCategory().getId();
        LocalDate date = transaction.getDate();
        TransactionType type = transaction.getType();

        transaction.softDelete();
        transactionRepository.save(transaction);

        if (type == TransactionType.EXPENSE) {
            recalculateBudgetAlerts(userId, categoryId, date.getYear(), date.getMonthValue());
        }
    }

    public BigDecimal getTotalIncomeByPeriod(Long userId, LocalDate startDate, LocalDate endDate) {

        validateDateRange(startDate, endDate);

        BigDecimal total = transactionRepository.sumIncomeByPeriod(userId, startDate, endDate);
        return total != null ? total : BigDecimal.ZERO;
    }

    public BigDecimal getTotalExpenseByPeriod(Long userId, LocalDate startDate, LocalDate endDate) {
        validateDateRange(startDate, endDate);

        BigDecimal total = transactionRepository.sumExpensesByPeriod(userId, startDate, endDate);
        return total != null ? total : BigDecimal.ZERO;
    }

    public List<CategoryExpenseDTO> getExpensesByCategory(Long userId, LocalDate startDate, LocalDate endDate) {
        validateDateRange(startDate, endDate);

        var projections = transactionRepository.findExpensesByCategory(userId, startDate, endDate);

        BigDecimal total = projections.stream()
                .map(TransactionRepository.CategoryExpenseProjection::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return projections.stream()
                .map(p -> {
                    BigDecimal percentage = BigDecimal.ZERO;
                    if (total.compareTo(BigDecimal.ZERO) > 0) {
                        percentage = p.getTotalAmount()
                                .multiply(BigDecimal.valueOf(100))
                                .divide(total, 2, RoundingMode.HALF_UP);
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

    private void validateTransactionOwnership(Transaction transaction, Long userId) {
        if (!transaction.getUser().getId().equals(userId)) {
            throw new UnauthorizedAcessException("Acesso não autorizado: Transação não pertence a este usuário");
        }
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new InvalidDateException("Data inicial e final são obrigatórias");
        }
        if (startDate.isAfter(endDate)) {
            throw new InvalidDateRangeException("Data inicial não pode ser maior que data final");
        }
    }

    private TransactionResponseDTO toDTO(Transaction transaction) {
        return new TransactionResponseDTO(
                transaction.getId(),
                transaction.getDescription(),
                transaction.getAmount(),
                transaction.getDate(),
                transaction.getType(),
                transaction.getCategory().getId(),
                transaction.getCategory().getName(),
                transaction.getPaymentMethod(),
                transaction.getNotes(),
                transaction.getCreatedAt(),
                transaction.getUpdatedAt()
        );
    }

    /**
     * Verifica orçamento e cria alerta se necessário
     * Regra de Negócio:
     * 1. Buscar budget da categoria no mês/ano
     * 2. Se existe budget:
     *    a. Calcular total gasto no mês
     *    b. Calcular percentual: (gasto / orçamento) * 100
     *    c. Se percentual >= 80% E < 100%: AlertService.createWarningAlert()
     *    d. Se percentual >= 100%: AlertService.createCriticalAlert()
     */
    private void checkBudgetAndCreateAlert(Long userId, Long categoryId, int year, int month) {
        List<Budget> budgets = budgetRepository.findByUserIdAndMonthAndYear(userId, month, year);

        if (budgets != null && !budgets.isEmpty()) {
            for (Budget budget : budgets) {
                if (budget.getCategory().getId().equals(categoryId)) {
                    BigDecimal spent = transactionRepository.sumExpensesByCategoryAndMonth(
                            userId, categoryId, year, month);

                    if (spent == null) {
                        spent = BigDecimal.ZERO;
                    }

                    alertService.checkAndSendBudgetAlert(budget, spent);
                }
            }
        }
    }

    private void recalculateBudgetAlerts(Long userId, Long categoryId, int year, int month) {
        checkBudgetAndCreateAlert(userId, categoryId, year, month);
    }
}

