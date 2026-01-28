package br.com.moneyflow.model.dto.category;

import br.com.moneyflow.model.entity.CategoryType;

import java.time.LocalDateTime;

public record CategoryResponseDTO(Long id,
                                  String name,
                                  String description,
                                  CategoryType type,
                                  String color,
                                  String icon,
                                  LocalDateTime createdAt,
                                  LocalDateTime updatedAt) {
}
