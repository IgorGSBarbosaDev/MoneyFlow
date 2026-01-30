package br.com.moneyflow.model.dto.transaction;

import br.com.moneyflow.model.entity.PaymentMethod;
import br.com.moneyflow.model.entity.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionRequestDTO (String description,
                                     BigDecimal amount,
                                     LocalDate date,
                                     TransactionType type,
                                     Long categoryId,
                                     String notes,
                                     PaymentMethod paymentMethod) {
}
