package br.com.moneyflow.model.dto.auth;

import br.com.moneyflow.model.dto.user.UserResponseDTO;

public record AuthResponseDTO(
    String token,
    String type,
    Long expiresIn,
    UserResponseDTO user
) {
    public AuthResponseDTO(String token, Long expiresIn, UserResponseDTO user) {
        this(token, "Bearer", expiresIn, user);
    }
}
