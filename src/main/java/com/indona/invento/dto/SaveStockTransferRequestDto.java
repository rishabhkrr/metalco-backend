package com.indona.invento.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaveStockTransferRequestDto {

    private Long stockTransferId;
    private Long grnId;
    private String transferStage; // e.g., "COMPLETED"
    private String updatedBy;
}

