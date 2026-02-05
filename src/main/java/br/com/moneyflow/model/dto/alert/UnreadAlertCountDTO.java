package br.com.moneyflow.model.dto.alert;

public record UnreadAlertCountDTO(
        Long userId,
        Long unreadCount
) {
}
