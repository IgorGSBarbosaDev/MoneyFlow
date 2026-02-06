package br.com.moneyflow.model.dto.dashboard;

import java.util.List;

public record MonthlyComparisonDTO(
        MonthDataDTO currentMonth,
        MonthDataDTO previousMonth,
        VariationDTO incomeVariation,
        VariationDTO expenseVariation,
        VariationDTO balanceVariation,
        List<CategoryVariationDTO> categoriesWithBiggestIncrease,
        List<CategoryVariationDTO> categoriesWithBiggestDecrease
) {
}
