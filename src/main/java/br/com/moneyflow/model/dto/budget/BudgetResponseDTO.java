package br.com.moneyflow.model.dto.budget;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BudgetResponseDTO(
        Long id,
        Long categoryId,
        String categoryName,
        BigDecimal amount,
        Integer month,
        Integer year,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
