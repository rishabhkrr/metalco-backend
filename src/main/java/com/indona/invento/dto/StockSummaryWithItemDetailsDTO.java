package com.indona.invento.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
public class StockSummaryWithItemDetailsDTO {

    private String itemDescription;
    private String brand;
    private String grade;
    private String temper;
    private BigDecimal quantityKg;

    private BigDecimal moq;
    private Integer leadTimeDays;

    private String productCategory;
    private String materialType;

    private List<MonthlySaleDTO> last12MonthsSales;

    private String unit;

    private BigDecimal totalConsumption;
    private BigDecimal monthlyAverageConsumption;
    private BigDecimal dailyConsumption;
    private BigDecimal safetyStock;
    private BigDecimal reorderQuantity;
    private BigDecimal reorderLevel;
    private String status;


}
