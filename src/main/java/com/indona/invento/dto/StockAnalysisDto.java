package com.indona.invento.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockAnalysisDto {
    private String itemDescription;
    private String unit;
    private BigDecimal currentStock;
    private List<MonthlySaleDTO> last6MonthsSales;
    private BigDecimal totalConsumption;
    private BigDecimal monthlyAverageConsumption;
    private BigDecimal dailyConsumption;
    private BigDecimal safetyStock;
    private BigDecimal reorderQuantity;
    private BigDecimal reorderLevel;
}

