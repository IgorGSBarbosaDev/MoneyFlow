package br.com.moneyflow.model.dto.user;

import jakarta.validation.constraints.*;

public record UserRegistrationDTO(
    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres")
    String name,

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ter formato válido")
    @Size(max = 120, message = "Email deve ter no máximo 120 caracteres")
    String email,

    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 8, message = "Senha deve ter no mínimo 8 caracteres")
    String password)
{
    public UserRegistrationDTO {
        name = name != null ? name.trim() : null;
        email = email != null ? email.trim().toLowerCase() : null;
        // Senha não sofre trim para preservar espaços intencionais
    }
}
