package br.com.moneyflow.model.dto.transaction;

import br.com.moneyflow.model.entity.PaymentMethod;
import br.com.moneyflow.model.entity.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record TransactionResponseDTO (Long id,
                                      String description,
                                      BigDecimal amount,
                                      LocalDate date,
                                      TransactionType type,
                                      Long categoryId,
                                      String categoryName,
                                      PaymentMethod paymentMethod,
                                      String notes,
                                      LocalDateTime createdAt,
                                      LocalDateTime updatedAt) {
}
