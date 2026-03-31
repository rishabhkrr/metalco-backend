package com.indona.invento.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaterialRequestDTO {
    private String unitCode;
    private String unitName;
    private String requestingUnit;
    private String deliveryAddress;
    private String requestingUnitUnitCode;

    private List<MaterialRequestItemDTO> items;
}


