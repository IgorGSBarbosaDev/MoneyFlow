package br.com.moneyflow.model.dto.category;

import br.com.moneyflow.model.entity.CategoryType;

public record CategoryRequestDTO (String name,
                                  String description,
                                  CategoryType type,
                                  String color,
                                  String icon) {

}
