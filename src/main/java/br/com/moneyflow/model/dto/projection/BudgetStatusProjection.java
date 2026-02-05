package br.com.moneyflow.model.dto.projection;

import java.math.BigDecimal;
import java.math.RoundingMode;

public interface BudgetStatusProjection {
    Long getId();
    Long getCategoryId();
    String getCategoryName();
    BigDecimal getBudgetAmount();
    BigDecimal getSpentAmount();

    default BigDecimal getRemainingAmount() {
        BigDecimal spent = getSpentAmount() != null ? getSpentAmount() : BigDecimal.ZERO;
        return getBudgetAmount().subtract(spent);
    }

    default BigDecimal getPercentageUsed() {
        if (getBudgetAmount() == null || getBudgetAmount().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal spent = getSpentAmount() != null ? getSpentAmount() : BigDecimal.ZERO;
        return spent
                .multiply(BigDecimal.valueOf(100))
                .divide(getBudgetAmount(), 2, RoundingMode.HALF_UP);
    }
}
