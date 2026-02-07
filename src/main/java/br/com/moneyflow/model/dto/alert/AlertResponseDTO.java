package br.com.moneyflow.model.dto.alert;

import br.com.moneyflow.model.dto.category.CategorySimpleDTO;
import br.com.moneyflow.model.entity.AlertLevel;
import br.com.moneyflow.model.entity.AlertType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AlertResponseDTO(
        Long id,
        String title,
        String message,
        AlertLevel level,
        AlertType type,
        Boolean isRead,
        BigDecimal percentage,
        CategorySimpleDTO category,
        LocalDateTime createdAt,
        LocalDateTime readAt
) {
}
