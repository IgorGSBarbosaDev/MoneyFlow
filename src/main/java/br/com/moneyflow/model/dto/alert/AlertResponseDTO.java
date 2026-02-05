package br.com.moneyflow.model.dto.alert;

import br.com.moneyflow.model.entity.AlertLevel;
import br.com.moneyflow.model.entity.AlertType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AlertResponseDTO(
        Long id,
        String message,
        AlertLevel level,
        AlertType alertType,
        Long categoryId,
        String categoryName,
        Long budgetId,
        BigDecimal budgetAmount,
        BigDecimal currentAmount,
        BigDecimal percentageUsed,
        Integer month,
        Integer year,
        Boolean read,
        LocalDateTime readAt,
        LocalDateTime createdAt
) {
}
