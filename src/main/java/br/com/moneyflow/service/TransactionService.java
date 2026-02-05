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

    public TransactionResponseDTO getTransactionById(Long userId, Long transactionId){
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found with id: " + transactionId));

        validadeTransactionOwnership(transaction, userId);

        return toDTO(transaction);
    }

    private void validadeTransactionOwnership(Transaction transaction, Long userId ) {
        if (!transaction.getUser().getId().equals(userId)) {
            throw new UnauthorizedAcessException("Unauthorized access: Transaction does not belong to this user");
        }
    }
    private TransactionResponseDTO toDTO(Transaction transaction) {
        return new TransactionResponseDTO(transaction.getId(),
                transaction.getDescription(),
                transaction.getAmount(),
                transaction.getDate(),
                transaction.getType(),
                transaction.getCategory().getId(),
                transaction.getCategory().getName(),
                transaction.getPaymentMethod(),
                transaction.getNotes(),
                transaction.getCreatedAt(),
                transaction.getUpdatedAt());
    }
    private void recalculateBudgetAndMaybeAlert(Transaction transaction) {
        List<Budget> budgets = budgetRepository.findByUserIdAndMonthAndYear(
                transaction.getUser().getId(),
                transaction.getDate().getMonthValue(),
                transaction.getDate().getYear()
        );

        if (budgets != null && !budgets.isEmpty()) {
            for (Budget budget : budgets) {
                BigDecimal spent = transactionRepository.sumExpensesByCategoryAndMonth(
                        budget.getUser().getId(),
                        budget.getCategory().getId(),
                        budget.getYear(),
                        budget.getMonth()
                );

                alertService.checkAndSendBudgetAlert(budget, spent);
            }
        }
    }

}

