package br.com.moneyflow.model.dto.budget;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record BudgetRequestDTO(
        @NotNull(message = "Category ID is required")
        Long categoryId,

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be greater than zero")
        BigDecimal amount,

        @NotNull(message = "Month is required")
        @Min(value = 1, message = "Month must be between 1 and 12")
        @Max(value = 12, message = "Month must be between 1 and 12")
        Integer month,

        @NotNull(message = "Year is required")
        @Min(value = 2000, message = "Year must be 2000 or later")
        Integer year
) {
}
