package com.indona.invento.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HsnCodeMasterDto {
    private String materialType;
    private String productCategory;
    private String hsnCode;
    private String description;
    private String previousHsnCode;
    private LocalDate effectiveDate;
    private String gstRate;        // GST Rate (e.g., 5.0, 12.0, 18.0)
    private LocalDate gstEffectiveDate; // GST Effective Date
    private String status;             // Pending / APPROVED / REJECTED (default: Pending)
    private String lastgstRate;
}
