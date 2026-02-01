package br.com.moneyflow.service;

import br.com.moneyflow.exception.TransactionNotFoundException;
import br.com.moneyflow.exception.UnauthorizedAcessException;
import br.com.moneyflow.model.dto.transaction.TransactionResponseDTO;
import br.com.moneyflow.model.entity.Transaction;
import br.com.moneyflow.repository.BudgetRepository;
import br.com.moneyflow.repository.CategoryRepository;
import br.com.moneyflow.repository.TransactionRepository;
import br.com.moneyflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private TransactionRepository transactionRepository;
    private CategoryRepository categoryRepository;
    private UserRepository userRepository;
    private BudgetRepository budgetRepository;
    private AlertService alertService;



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
