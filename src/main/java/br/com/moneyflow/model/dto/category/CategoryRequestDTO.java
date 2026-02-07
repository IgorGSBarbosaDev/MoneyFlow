package br.com.moneyflow.model.dto.category;

import br.com.moneyflow.model.entity.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CategoryRequestDTO (
        @NotBlank(message = "Nome da categoria é obrigatório")
        @Size(min = 2, max = 50, message = "Nome deve ter entre 2 e 50 caracteres")
        String name,

        String description,

        @NotNull(message = "Tipo da categoria é obrigatório")
        CategoryType type,

        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Cor deve estar no formato hexadecimal (#RRGGBB)")
        String color,

        String icon) {

}
