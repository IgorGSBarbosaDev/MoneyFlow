package br.com.moneyflow.model.dto.user;

import jakarta.validation.constraints.*;

public record ChangePasswordDTO(
    @NotBlank(message = "Senha atual é obrigatória")
    String currentPassword,

    @NotBlank(message = "Nova senha é obrigatória")
    @Size(min = 8, message = "Nova senha deve ter no mínimo 8 caracteres")
    String newPassword,

    @NotBlank(message = "Confirmação de senha é obrigatória")
    String confirmPassword) {

}
