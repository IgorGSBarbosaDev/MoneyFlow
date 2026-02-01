package br.com.moneyflow.service;

import br.com.moneyflow.exception.TransactionNotFoundException;
import br.com.moneyflow.exception.UnauthorizedAcessException;
import br.com.moneyflow.model.dto.transaction.TransactionResponseDTO;
import br.com.moneyflow.model.entity.Transaction;
import br.com.moneyflow.model.entity.TransactionType;
import br.com.moneyflow.repository.BudgetRepository;
import br.com.moneyflow.repository.CategoryRepository;
import br.com.moneyflow.repository.TransactionRepository;
import br.com.moneyflow.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

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
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        var category = categoryRepository.findByUserIdAndCategoryId(userId, transactionRequestDTO.categoryId())
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with id: " + transactionRequestDTO.categoryId()));

        if (!transactionRequestDTO.type().equals(category.getType())){
            throw new CategoryNotFoundException("Category type does not match transaction type");
        }
        if (transactionRequestDTO.amount() == null || transactionRequestDTO.amount().compareTo(java.math.BigDecimal.ZERO)<= 0){
            throw new InvalidAmountException("Amount must be greater than 0");
        }
        if (transactionRequestDTO.date().isAfter(LocalDate.now())){
            throw new InvalidDateException("Transaction date cannot be in the future");
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

        if (savedTransaction.getType() == TransactionType.EXPENSE){
            recalculateBudgetAndMaybeAlert(savedTransaction);
        }
        return toDTO(savedTransaction);
    }

    public TransactionResponseDTO getTransactionById(Long userId, Long transactionId){
        transactionRepository.findById(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found with id: " + transactionId));

        Transaction transaction = transactionRepository.findById(transactionId).get();
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

}
