package br.com.moneyflow.controller;

import br.com.moneyflow.model.dto.dashboard.MonthlyComparisonDTO;
import br.com.moneyflow.model.dto.dashboard.MonthlySummaryDTO;
import br.com.moneyflow.model.dto.dashboard.YearlyOverviewDTO;
import br.com.moneyflow.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/{userId}/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<MonthlySummaryDTO> getMonthlySummary(
            @PathVariable Long userId,
            @RequestParam Integer month,
            @RequestParam Integer year) {
        MonthlySummaryDTO summary = dashboardService.getMonthlySummary(userId, month, year);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/comparison")
    public ResponseEntity<MonthlyComparisonDTO> getMonthlyComparison(
            @PathVariable Long userId,
            @RequestParam Integer month,
            @RequestParam Integer year) {
        MonthlyComparisonDTO comparison = dashboardService.getMonthlyComparison(userId, month, year);
        return ResponseEntity.ok(comparison);
    }

    @GetMapping("/yearly")
    public ResponseEntity<YearlyOverviewDTO> getYearlyOverview(
            @PathVariable Long userId,
            @RequestParam Integer year) {
        YearlyOverviewDTO overview = dashboardService.getYearlyOverview(userId, year);
        return ResponseEntity.ok(overview);
    }
}
