package com.indona.invento.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MaterialTransferScheduleDto {
    private Long id;
    private String mrNumber;
    private String unitCode;
    private String unitName;
    private String requestingUnit;
    private String deliveryAddress;
    private String status;
    private LocalDateTime timestamp;

    private String itemDescription;
    private String materialType;
    private String productCategory;
    private String brand;
    private String grade;
    private String temper;
    private Integer requiredQuantity;
    private String uom;
    private String itemStatus;
    private String lineNumber;
    private BigDecimal retrievalQuantityKg;
    private Integer retrievalQuantityNo;
    private String storageArea;
    private String requestingUnitUnitCode;
}
