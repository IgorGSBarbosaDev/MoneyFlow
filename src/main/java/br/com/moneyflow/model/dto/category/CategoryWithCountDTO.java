package br.com.moneyflow.model.dto.category;

import java.time.LocalDateTime;

public record CategoryWithCountDTO(Long id,
                                   String name,
                                   String description,
                                   String type,
                                   String color,
                                   String icon,
                                   LocalDateTime createdAt,
                                   LocalDateTime updatedAt,
                                   Long transactionCount) {
}
