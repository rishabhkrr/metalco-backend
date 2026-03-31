package com.indona.invento.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RackBinStorageQtyUpdateDto {

    private String storageType;
    private String storageArea;
    private String rackNo;
    private String columnNo;
    private String binNo;
    private Double currentStorageQtyChange;
    private String action;
    private String unit;
}
