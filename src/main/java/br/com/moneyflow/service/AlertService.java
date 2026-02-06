package br.com.moneyflow.service;

import br.com.moneyflow.exception.resource.AlertNotFoundException;
import br.com.moneyflow.exception.authorization.UnauthorizedAcessException;
import br.com.moneyflow.exception.business.ValidationException;
import br.com.moneyflow.model.dto.alert.AlertResponseDTO;
import br.com.moneyflow.model.entity.*;
import br.com.moneyflow.repository.AlertRepository;
import br.com.moneyflow.repository.BudgetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertRepository alertRepository;
    private final BudgetRepository budgetRepository;

    @Transactional
    public void checkAndSendBudgetAlert(Budget budget, BigDecimal currentSpent) {
        if (budget == null || currentSpent == null) {
            return;
        }

        BigDecimal budgetAmount = budget.getAmount();
        if (budgetAmount.compareTo(BigDecimal.ZERO) == 0) {
            return;
        }

        BigDecimal percentage = calculatePercentage(currentSpent, budgetAmount);

        if (percentage.compareTo(BigDecimal.valueOf(100)) >= 0) {
            createBudgetCriticalAlert(budget.getUser().getId(), budget.getId(), percentage);
        } else if (percentage.compareTo(BigDecimal.valueOf(80)) >= 0) {
            createBudgetWarningAlert(budget.getUser().getId(), budget.getId(), percentage);
        } else {
            removeExistingBudgetAlerts(budget);
        }
    }

    @Transactional
    public AlertResponseDTO createBudgetWarningAlert(Long userId, Long budgetId, BigDecimal percentage) {
        if (alertRepository.existsByUserIdAndBudgetIdAndLevel(userId, budgetId, AlertLevel.WARNING)) {
            return null;
        }

        Budget budget = budgetRepository.findByIdAndUserId(budgetId, userId)
                .orElseThrow(() -> new ValidationException("Orçamento não encontrado"));

        removeExistingBudgetAlerts(budget);

        String message = String.format(
                "Você gastou %.2f%% do orçamento de %s este mês",
                percentage,
                budget.getCategory().getName());

        Alert alert = Alert.builder()
                .message(message)
                .level(AlertLevel.WARNING)
                .alertType(AlertType.BUDGET_WARNING)
                .budgetAmount(budget.getAmount())
                .currentAmount(calculateCurrentAmount(budget.getAmount(), percentage))
                .month(budget.getMonth())
                .year(budget.getYear())
                .category(budget.getCategory())
                .budget(budget)
                .user(budget.getUser())
                .read(false)
                .build();

        Alert savedAlert = alertRepository.save(alert);
        return toAlertResponseDTO(savedAlert);
    }

    @Transactional
    public AlertResponseDTO createBudgetCriticalAlert(Long userId, Long budgetId, BigDecimal percentage) {
        if (alertRepository.existsByUserIdAndBudgetIdAndLevel(userId, budgetId, AlertLevel.CRITICAL)) {
            return null;
        }

        Budget budget = budgetRepository.findByIdAndUserId(budgetId, userId)
                .orElseThrow(() -> new ValidationException("Orçamento não encontrado"));

        removeExistingBudgetAlerts(budget);

        BigDecimal exceededBy = percentage.subtract(BigDecimal.valueOf(100));
        String message = String.format(
                "ATENÇÃO! Orçamento de %s excedido em %.2f%%",
                budget.getCategory().getName(),
                exceededBy.compareTo(BigDecimal.ZERO) > 0 ? exceededBy : BigDecimal.ZERO);

        Alert alert = Alert.builder()
                .message(message)
                .level(AlertLevel.CRITICAL)
                .alertType(AlertType.BUDGET_EXCEEDED)
                .budgetAmount(budget.getAmount())
                .currentAmount(calculateCurrentAmount(budget.getAmount(), percentage))
                .month(budget.getMonth())
                .year(budget.getYear())
                .category(budget.getCategory())
                .budget(budget)
                .user(budget.getUser())
                .read(false)
                .build();

        Alert savedAlert = alertRepository.save(alert);
        return toAlertResponseDTO(savedAlert);
    }

    public List<AlertResponseDTO> getAlertsByUser(Long userId, Boolean isRead) {
        List<Alert> alerts = alertRepository.findByUserIdOrderedByPriorityAndDate(userId, isRead);

        return alerts.stream()
                .map(this::toAlertResponseDTO)
                .collect(Collectors.toList());
    }

    public AlertResponseDTO getAlertById(Long userId, Long alertId) {
        Alert alert = alertRepository.findByIdAndUserId(alertId, userId)
                .orElseThrow(() -> new AlertNotFoundException("Alerta não encontrado com id: " + alertId));

        return toAlertResponseDTO(alert);
    }

    public Long getUnreadAlertCount(Long userId) {
        return alertRepository.countByUserIdAndReadFalse(userId);
    }

    @Transactional
    public AlertResponseDTO markAlertAsRead(Long userId, Long alertId) {
        Alert alert = alertRepository.findByIdAndUserId(alertId, userId)
                .orElseThrow(() -> new AlertNotFoundException("Alerta não encontrado com id: " + alertId));

        if (Boolean.TRUE.equals(alert.getRead())) {
            return toAlertResponseDTO(alert);
        }

        alert.markAsRead();
        Alert updatedAlert = alertRepository.save(alert);

        return toAlertResponseDTO(updatedAlert);
    }

    @Transactional
    public int markMultipleAlertsAsRead(Long userId, List<Long> alertIds) {
        if (alertIds == null || alertIds.isEmpty()) {
            return 0;
        }

        long count = alertRepository.countByUserIdAndIdIn(userId, alertIds);
        if (count != alertIds.size()) {
            throw new UnauthorizedAcessException(
                    "Um ou mais alertas não pertencem ao usuário ou não existem");
        }

        return alertRepository.markAlertsAsRead(userId, alertIds, LocalDateTime.now());
    }

    @Transactional
    public void deleteAlert(Long userId, Long alertId) {
        Alert alert = alertRepository.findByIdAndUserId(alertId, userId)
                .orElseThrow(() -> new AlertNotFoundException("Alerta não encontrado com id: " + alertId));

        alertRepository.delete(alert);
    }

    @Transactional
    public int cleanOldReadAlerts(Long userId, Integer daysOld) {
        if (daysOld == null || daysOld < 1) {
            daysOld = 30; // Default: 30 dias
        }

        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        return alertRepository.deleteOldReadAlerts(userId, cutoffDate);
    }

    private void removeExistingBudgetAlerts(Budget budget) {
        alertRepository.deleteByUserIdAndBudgetIdAndLevel(
                budget.getUser().getId(),
                budget.getId(),
                AlertLevel.WARNING);

        alertRepository.deleteByUserIdAndBudgetIdAndLevel(
                budget.getUser().getId(),
                budget.getId(),
                AlertLevel.CRITICAL);
    }

    private BigDecimal calculatePercentage(BigDecimal currentSpent, BigDecimal budgetAmount) {
        if (budgetAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return currentSpent
                .multiply(BigDecimal.valueOf(100))
                .divide(budgetAmount, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateCurrentAmount(BigDecimal budgetAmount, BigDecimal percentage) {
        return budgetAmount
                .multiply(percentage)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    private AlertResponseDTO toAlertResponseDTO(Alert alert) {
        BigDecimal percentageUsed = BigDecimal.ZERO;
        if (alert.getBudgetAmount() != null &&
            alert.getBudgetAmount().compareTo(BigDecimal.ZERO) > 0 &&
            alert.getCurrentAmount() != null) {
            percentageUsed = calculatePercentage(alert.getCurrentAmount(), alert.getBudgetAmount());
        }

        return new AlertResponseDTO(
                alert.getId(),
                alert.getMessage(),
                alert.getLevel(),
                alert.getAlertType(),
                alert.getCategory() != null ? alert.getCategory().getId() : null,
                alert.getCategory() != null ? alert.getCategory().getName() : null,
                alert.getBudget() != null ? alert.getBudget().getId() : null,
                alert.getBudgetAmount(),
                alert.getCurrentAmount(),
                percentageUsed,
                alert.getMonth(),
                alert.getYear(),
                alert.getRead(),
                alert.getReadAt(),
                alert.getCreatedAt()
        );
    }
}

