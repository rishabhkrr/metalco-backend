package com.indona.invento.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CurrentPriceSummaryDto {
    private String lastInvoiceDate; // null
    private Double lastSoldPrice;   // null
    private Double nalcoPriceOnLastInvoiceDate; // null
    private Double currentNalcoPrice;
    private Double hindalcoPriceOnLastInvoiceDate; // null
    private Double currentHindalcoPrice;
}
