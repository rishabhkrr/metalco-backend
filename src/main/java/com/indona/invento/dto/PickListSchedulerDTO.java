package com.indona.invento.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PickListSchedulerDTO {
    private String unit;
    private String soNumber;
    private String lineNumber;
    private String nextProcess;
    private String orderType;
    private String productCategory;
    private String itemDescription;
    private String brand;
    private BigDecimal retrievalQuantityKg;
    private Integer retrievalQuantityNo;
    private String storageArea;
    private String rackColumnShelfNumber;
}
