package br.com.moneyflow.controller;

import br.com.moneyflow.config.security.CurrentUser;
import br.com.moneyflow.model.dto.alert.*;
import br.com.moneyflow.service.AlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/alerts")
@Tag(name = "Alerts", description = "Gerenciamento de alertas")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    @GetMapping
    @Operation(summary = "Listar alertas do usuário")
    @ApiResponse(responseCode = "200", description = "Lista de alertas")
    public ResponseEntity<List<AlertResponseDTO>> list(
            @CurrentUser Long userId,
            @RequestParam(required = false) Boolean isRead) {
        List<AlertResponseDTO> alerts = alertService.getAlertsByUser(userId, isRead);
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Contar alertas não lidos")
    @ApiResponse(responseCode = "200", description = "Contagem de alertas não lidos")
    public ResponseEntity<UnreadCountDTO> getUnreadCount(@CurrentUser Long userId) {
        Long count = alertService.getUnreadAlertCount(userId);
        return ResponseEntity.ok(new UnreadCountDTO(count));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Marcar alerta como lido")
    @ApiResponse(responseCode = "200", description = "Alerta marcado como lido")
    public ResponseEntity<AlertResponseDTO> markAsRead(
            @CurrentUser Long userId,
            @PathVariable Long id) {
        AlertResponseDTO alert = alertService.markAlertAsRead(userId, id);
        return ResponseEntity.ok(alert);
    }

    @PatchMapping("/read-all")
    @Operation(summary = "Marcar todos alertas como lidos")
    @ApiResponse(responseCode = "200", description = "Todos os alertas foram marcados como lidos")
    public ResponseEntity<MarkAllReadResponseDTO> markAllAsRead(@CurrentUser Long userId) {
        Integer updatedCount = alertService.markAllAlertsAsRead(userId);
        String message = updatedCount + " alertas marcados como lidos";
        return ResponseEntity.ok(new MarkAllReadResponseDTO(updatedCount, message));
    }

    @PatchMapping("/read-multiple")
    @Operation(summary = "Marcar múltiplos alertas como lidos")
    @ApiResponse(responseCode = "200", description = "Alertas marcados como lidos")
    public ResponseEntity<MarkMultipleReadResponseDTO> markMultipleAsRead(
            @CurrentUser Long userId,
            @Valid @RequestBody MarkMultipleReadRequestDTO dto) {
        Integer updatedCount = alertService.markMultipleAlertsAsRead(userId, dto.alertIds());
        String message = updatedCount + " alertas marcados como lidos";
        return ResponseEntity.ok(new MarkMultipleReadResponseDTO(updatedCount, message));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir alerta")
    @ApiResponse(responseCode = "204", description = "Alerta excluído")
    public ResponseEntity<Void> delete(
            @CurrentUser Long userId,
            @PathVariable Long id) {
        alertService.deleteAlert(userId, id);
        return ResponseEntity.noContent().build();
    }
}
