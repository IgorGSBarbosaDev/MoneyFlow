package br.com.moneyflow.model.dto.projection;

import java.math.BigDecimal;
import java.math.RoundingMode;

public interface BudgetStatusProjection {
    Long getId();
    Long getCategoryId();
    String getCategoryName();
    BigDecimal getBudgetAmount();
    BigDecimal getSpentAmount();

    default BigDecimal getRemainingAmount(){
        return getBudgetAmount().subtract(getSpentAmount());
    }

     default BigDecimal getPercentageUsed(){
        if (getBudgetAmount().compareTo(BigDecimal.ZERO) == 0){
            return BigDecimal.ZERO;
        }
        return getSpentAmount()
                .divide(getBudgetAmount(), 2, RoundingMode.HALF_UP)
                .multiply(new BigDecimal(100));

     }
}
