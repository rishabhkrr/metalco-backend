package com.indona.invento.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GRNApprovalRequestDTO {

    private Long grnId;                    // GRN ID to approve
    private String grnRefNo;               // GRN Reference Number (from frontend)

    // Default warehouse values
    private String unit;                   // Unit name


    // Multiple items list
    private List<GRNApprovalItemDTO> items; // Array of items to be added to StockSummary
}


