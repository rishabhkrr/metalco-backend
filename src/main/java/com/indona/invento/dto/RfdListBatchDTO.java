package com.indona.invento.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RfdListBatchDTO {
    private String id;
    private String itemDescription;
    private String itemDimension;
    private String batchNumber;
    private String dateOfInward;
    private BigDecimal quantityKg;
    private Integer quantityNo;
    private Map<String, Object> qrCodeFG;  // { "exists": true/false, "url": "..." }
}
