package com.indona.invento.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RackBinMasterDto {
    private String unitCode;
    private String unitName;
    private String storageType;
    private String storageArea;
    private String rackNo;
    private String columnNo;
    private String binNo;
    private String status;

    private String binCapacity;
    private Double distance;
    private String itemCategory;
    private Double currentStorage;
    private String qr;
    private Integer storageAreaOrder;
    private boolean automated;

}
