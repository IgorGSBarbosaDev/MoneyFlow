package br.com.moneyflow.controller;

import br.com.moneyflow.config.security.CurrentUser;
import br.com.moneyflow.model.dto.budget.BudgetRequestDTO;
import br.com.moneyflow.model.dto.budget.BudgetResponseDTO;
import br.com.moneyflow.model.dto.budget.BudgetStatusDTO;
import br.com.moneyflow.model.dto.budget.BudgetUpdateDTO;
import br.com.moneyflow.service.BudgetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/budgets")
@Tag(name = "Budgets", description = "Gerenciamento de orçamentos mensais")
@RequiredArgsConstructor
@Validated
public class BudgetController {

    private final BudgetService budgetService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Definir orçamento mensal para categoria")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Orçamento criado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "409", description = "Orçamento já existe para esta categoria/período")
    })
    public ResponseEntity<BudgetResponseDTO> create(
            @CurrentUser Long userId,
            @Valid @RequestBody BudgetRequestDTO dto) {
        BudgetResponseDTO response = budgetService.createBudget(userId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar orçamentos do usuário")
    @ApiResponse(responseCode = "200", description = "Lista de orçamentos")
    public ResponseEntity<List<BudgetResponseDTO>> list(
            @CurrentUser Long userId,
            @RequestParam(required = false) @Min(1) @Max(12) Integer month,
            @RequestParam(required = false) @Min(2000) Integer year) {
        List<BudgetResponseDTO> response = budgetService.getBudgets(userId, month, year);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar orçamento por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Orçamento encontrado"),
            @ApiResponse(responseCode = "404", description = "Orçamento não encontrado")
    })
    public ResponseEntity<BudgetResponseDTO> getById(
            @CurrentUser Long userId,
            @PathVariable Long id) {
        BudgetResponseDTO response = budgetService.getBudgetById(userId, id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/status")
    @Operation(summary = "Obter status detalhado do orçamento")
    @ApiResponse(responseCode = "200", description = "Status do orçamento")
    public ResponseEntity<BudgetStatusDTO> getStatus(
            @CurrentUser Long userId,
            @PathVariable Long id) {
        BudgetStatusDTO response = budgetService.getBudgetStatus(userId, id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/monthly-status")
    @Operation(summary = "Obter status de todos orçamentos do mês")
    @ApiResponse(responseCode = "200", description = "Lista de status dos orçamentos")
    public ResponseEntity<List<BudgetStatusDTO>> getMonthlyStatus(
            @CurrentUser Long userId,
            @RequestParam @Min(1) @Max(12) Integer month,
            @RequestParam @Min(2000) Integer year) {
        List<BudgetStatusDTO> response = budgetService.getMonthlyStatus(userId, month, year);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar valor do orçamento")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Orçamento atualizado"),
            @ApiResponse(responseCode = "404", description = "Orçamento não encontrado")
    })
    public ResponseEntity<BudgetResponseDTO> update(
            @CurrentUser Long userId,
            @PathVariable Long id,
            @Valid @RequestBody BudgetUpdateDTO dto) {
        BudgetResponseDTO response = budgetService.updateBudget(userId, id, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir orçamento")
    @ApiResponse(responseCode = "204", description = "Orçamento excluído")
    public ResponseEntity<Void> delete(
            @CurrentUser Long userId,
            @PathVariable Long id) {
        budgetService.deleteBudget(userId, id);
        return ResponseEntity.noContent().build();
    }
}
