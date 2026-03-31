package com.indona.invento.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InitiateStockTransferResponseDto {
    private Long stockTransferId;
    private String transferNumber;
    private Long grnId;
    private String grnRefNumber;
    private String invoiceNumber;
    private String poNumber;
    private String supplierName;
    private String supplierCode;
    private String unit;
    private String binStatus;
    private Date createdAt;
    private String message;
    private Boolean success;
}
