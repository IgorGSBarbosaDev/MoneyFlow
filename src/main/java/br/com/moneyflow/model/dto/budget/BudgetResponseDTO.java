package br.com.moneyflow.model.dto.budget;

import br.com.moneyflow.model.dto.category.CategorySimpleDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BudgetResponseDTO(
        Long id,
        CategorySimpleDTO category,
        BigDecimal amount,
        Integer month,
        Integer year,
        LocalDateTime createdAt
) {
}
