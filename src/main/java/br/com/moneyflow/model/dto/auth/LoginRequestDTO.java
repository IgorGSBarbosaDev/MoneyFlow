package br.com.moneyflow.model.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ter formato válido")
    String email,

    @NotBlank(message = "Senha é obrigatória")
    String password
) {
    public LoginRequestDTO {
        email = email != null ? email.trim().toLowerCase() : null;
    }
}
