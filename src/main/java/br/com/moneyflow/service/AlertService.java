package br.com.moneyflow.service;

import br.com.moneyflow.model.entity.Alert;
import br.com.moneyflow.model.entity.AlertLevel;
import br.com.moneyflow.model.entity.Budget;
import br.com.moneyflow.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class AlertService {
    private final AlertRepository alertRepository;

    public void checkAndSendBudgetAlert(Budget budget, BigDecimal currentSpent) {
        if (budget == null || currentSpent == null) {
            return;
        }

        BigDecimal budgetAmount = budget.getAmount();
        if (budgetAmount.compareTo(BigDecimal.ZERO) == 0) {
            return;
        }

        BigDecimal percentage = currentSpent
                .divide(budgetAmount, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        if (percentage.compareTo(BigDecimal.valueOf(100)) >= 0) {
            createOrUpdateAlert(budget, AlertLevel.CRITICAL, currentSpent, percentage);
        } else if (percentage.compareTo(BigDecimal.valueOf(80)) >= 0) {
            createOrUpdateAlert(budget, AlertLevel.WARNING, currentSpent, percentage);
        } else {
            verifyToRemoveExistingAlerts(budget);
        }
    }

    private void createOrUpdateAlert(Budget budget, AlertLevel level, BigDecimal currentSpent, BigDecimal percentage) {
        boolean exists = alertRepository.existsByUserIdAndBudgetIdAndLevel(
                budget.getUser().getId(),
                budget.getId(),
                level);

        if (exists) {
            return;
        }

        verifyToRemoveExistingAlerts(budget);

        String message = String.format(
                "Budget %s: %.2f%% used (%s of %s) for category %s",
                level.name(),
                percentage,
                currentSpent,
                budget.getAmount(),
                budget.getCategory().getName());

        Alert alert = new Alert(
                null,                        // id
                message,                        // message
                level,                          // level
                null,                           // alertType
                budget.getAmount(),             // budgetAmount
                currentSpent,                   // currentAmount
                budget.getMonth(),              // month
                budget.getYear(),               // year
                budget.getCategory(),           // category
                budget,                         // budget
                budget.getUser(),               // user
                false,                          // read
                null);                          // createdAt (será preenchido por @PrePersist)

        alertRepository.save(alert);
    }

    private void verifyToRemoveExistingAlerts(Budget budget) {
        alertRepository.deleteByUserIdAndBudgetIdAndLevel(
                budget.getUser().getId(),
                budget.getId(),
                AlertLevel.WARNING);

        alertRepository.deleteByUserIdAndBudgetIdAndLevel(
                budget.getUser().getId(),
                budget.getId(),
                AlertLevel.CRITICAL);
    }
}
