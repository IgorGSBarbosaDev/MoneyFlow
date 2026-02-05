package br.com.moneyflow.model.dto.transaction;

import br.com.moneyflow.model.entity.PaymentMethod;
import br.com.moneyflow.model.entity.TransactionType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionRequestDTO (
        @Size(min = 3, message = "Descrição deve ter no mínimo 3 caracteres")
        String description,

        @NotNull(message = "Valor é obrigatório")
        @Positive(message = "Valor deve ser positivo")
        BigDecimal amount,

        @NotNull(message = "Data é obrigatória")
        @PastOrPresent(message = "Data não pode ser futura")
        LocalDate date,

        @NotNull(message = "Tipo é obrigatório")
        TransactionType type,

        @NotNull(message = "Categoria é obrigatória")
        Long categoryId,

        String notes,

        PaymentMethod paymentMethod) {
}
