package br.com.moneyflow.controller;

import br.com.moneyflow.config.security.CurrentUser;
import br.com.moneyflow.model.dto.transaction.*;
import br.com.moneyflow.model.entity.TransactionType;
import br.com.moneyflow.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
@Tag(name = "Transactions", description = "Gerenciamento de transações financeiras")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar nova transação")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Transação criada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada")
    })
    public ResponseEntity<TransactionResponseDTO> create(
            @CurrentUser Long userId,
            @Valid @RequestBody TransactionRequestDTO dto) {
        TransactionResponseDTO response = transactionService.createTransaction(userId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar transação por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transação encontrada"),
            @ApiResponse(responseCode = "404", description = "Transação não encontrada")
    })
    public ResponseEntity<TransactionResponseDTO> getById(
            @CurrentUser Long userId,
            @PathVariable Long id) {
        TransactionResponseDTO response = transactionService.getTransactionById(userId, id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Listar transações com filtros e paginação")
    @ApiResponse(responseCode = "200", description = "Lista paginada de transações")
    public ResponseEntity<Page<TransactionResponseDTO>> list(
            @CurrentUser Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) TransactionType type,
            @PageableDefault(size = 20, sort = "date", direction = Sort.Direction.DESC) Pageable pageable) {

        TransactionFilterDTO filters = new TransactionFilterDTO(startDate, endDate, categoryId, type);
        Page<TransactionResponseDTO> response = transactionService.getTransactions(userId, filters, pageable);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar transação")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transação atualizada"),
            @ApiResponse(responseCode = "404", description = "Transação não encontrada")
    })
    public ResponseEntity<TransactionResponseDTO> update(
            @CurrentUser Long userId,
            @PathVariable Long id,
            @Valid @RequestBody TransactionRequestDTO dto) {
        TransactionResponseDTO response = transactionService.updateTransaction(userId, id, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir transação (soft delete)")
    @ApiResponse(responseCode = "204", description = "Transação excluída")
    public ResponseEntity<Void> delete(
            @CurrentUser Long userId,
            @PathVariable Long id) {
        transactionService.deleteTransaction(userId, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/summary")
    @Operation(summary = "Obter resumo de transações por período")
    public ResponseEntity<TransactionSummaryDTO> getSummary(
            @CurrentUser Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        TransactionSummaryDTO summary = transactionService.getTransactionSummary(userId, startDate, endDate);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/by-category")
    @Operation(summary = "Distribuição de gastos por categoria")
    public ResponseEntity<List<CategoryExpenseDTO>> getExpensesByCategory(
            @CurrentUser Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<CategoryExpenseDTO> expenses = transactionService.getExpensesByCategory(userId, startDate, endDate);
        return ResponseEntity.ok(expenses);
    }
}
