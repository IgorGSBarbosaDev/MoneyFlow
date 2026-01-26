package br.com.moneyflow.model.dto.user;

import java.time.LocalDateTime;

public record UserResponseDTO(
    Long id,
    String name,
    String email,
    Boolean active,
    LocalDateTime createdAt
) {}
