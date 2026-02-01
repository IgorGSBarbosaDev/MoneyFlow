package br.com.moneyflow.model.dto.transaction;

import java.math.BigDecimal;

public record CategoryExpenseDTO (Long categoryId,
                                  String categoryName,
                                  BigDecimal totalAmount,
                                  BigDecimal percentage,
                                  Long transactionCount) {
}
