package br.com.moneyflow.model.dto.category;

import br.com.moneyflow.model.entity.CategoryType;

public record CategorySimpleDTO(
        Long id,
        String name,
        CategoryType type) {
}
