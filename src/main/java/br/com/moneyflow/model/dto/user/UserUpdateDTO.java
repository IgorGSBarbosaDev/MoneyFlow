package br.com.moneyflow.model.dto.user;

import jakarta.validation.constraints.*;

public record UserUpdateDTO(
    @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres")
    String name,

    @Email(message = "Email deve ter formato válido")
    @Size(max = 120, message = "Email deve ter no máximo 120 caracteres")
    String email
) {
    public UserUpdateDTO {
        name = name != null ? name.trim() : null;
        email = email != null ? email.trim().toLowerCase() : null;
    }
}
