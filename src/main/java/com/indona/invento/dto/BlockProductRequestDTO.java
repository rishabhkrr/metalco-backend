package com.indona.invento.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlockProductRequestDTO {
    private String quotationNo;

    private String customerName;
    private String customerCode;
    private String customerEmail;
    private String customerPhone;

    private String unitId;
    private String unitName;
    private String unitCode;

    private List<ProductBlockDTO> products;
}
