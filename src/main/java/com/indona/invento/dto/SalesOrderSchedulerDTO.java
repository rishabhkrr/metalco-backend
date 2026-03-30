package com.indona.invento.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SalesOrderSchedulerDTO {

    private Integer slNo;
    private String nextProcess;
    private String soNumber;
    private String lineNumber;
    private String unit;
    private String primeCustomer;
    private String customerCode;
    private String customerName;
    private Boolean packing;
    private String orderType;
    private String productCategory;
    private String itemDescription;
    private String brand;
    private String grade;
    private String temper;
    private String dimension;
    private BigDecimal requiredQuantityKg;
    private Integer requiredQuantityNo;
    private LocalDate targetDateOfDispatch;
    private String pickListStatus;
    private String retrievalStatus;
    private String stockTransferStatus;
    private String customerCategory;
    private String uomKg;
    private String uomNo;
    private String productionStrategy;

    // This will be set during GET response
    private LocalDate planDate;

    private LocalDateTime completedTime;
}
