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
public class SaveStockTransferResponseDto {

    private Long stockTransferId;
    private String transferNumber;
    private String transferStage;
    private Long grnId;
    private String grnRefNumber;
    private String grnBinStatus;
    private Date updatedAt;
    private boolean success;
    private String message;
}

