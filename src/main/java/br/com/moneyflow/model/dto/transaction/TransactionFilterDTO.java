package br.com.moneyflow.model.dto.transaction;

import br.com.moneyflow.model.entity.TransactionType;

import java.time.LocalDate;

public record TransactionFilterDTO (LocalDate startDate,
                                    LocalDate endDate,
                                    Long categoryId,
                                    TransactionType type) {
}
