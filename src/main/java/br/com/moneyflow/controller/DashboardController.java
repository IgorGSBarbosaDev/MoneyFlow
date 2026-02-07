package br.com.moneyflow.controller;

import br.com.moneyflow.config.security.CurrentUser;
import br.com.moneyflow.model.dto.dashboard.MonthlyComparisonDTO;
import br.com.moneyflow.model.dto.dashboard.MonthlySummaryDTO;
import br.com.moneyflow.model.dto.dashboard.YearlyOverviewDTO;
import br.com.moneyflow.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/dashboard")
@Tag(name = "Dashboard", description = "Endpoints para visualizações e relatórios")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    @Operation(summary = "Obter resumo completo do mês")
    @ApiResponse(responseCode = "200", description = "Resumo mensal")
    public ResponseEntity<MonthlySummaryDTO> getMonthlySummary(
            @CurrentUser Long userId,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {

        if (month == null) {
            month = LocalDate.now().getMonthValue();
        }
        if (year == null) {
            year = LocalDate.now().getYear();
        }

        MonthlySummaryDTO summary = dashboardService.getMonthlySummary(userId, month, year);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/comparison")
    @Operation(summary = "Comparar mês atual com mês anterior")
    @ApiResponse(responseCode = "200", description = "Comparativo mensal")
    public ResponseEntity<MonthlyComparisonDTO> getMonthlyComparison(
            @CurrentUser Long userId,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {

        if (month == null) {
            month = LocalDate.now().getMonthValue();
        }
        if (year == null) {
            year = LocalDate.now().getYear();
        }

        MonthlyComparisonDTO comparison = dashboardService.getMonthlyComparison(userId, month, year);
        return ResponseEntity.ok(comparison);
    }

    @GetMapping("/yearly-overview")
    @Operation(summary = "Obter visão geral do ano")
    @ApiResponse(responseCode = "200", description = "Visão anual completa")
    public ResponseEntity<YearlyOverviewDTO> getYearlyOverview(
            @CurrentUser Long userId,
            @RequestParam(required = false) Integer year) {

        if (year == null) {
            year = LocalDate.now().getYear();
        }

        YearlyOverviewDTO overview = dashboardService.getYearlyOverview(userId, year);
        return ResponseEntity.ok(overview);
    }
}
