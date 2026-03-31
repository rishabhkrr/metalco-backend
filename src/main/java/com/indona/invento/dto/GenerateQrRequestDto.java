package com.indona.invento.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenerateQrRequestDto {

    private Long lineItemId;
    private String grnNumber;
    private String transferNumber;

    private String weighment;  // AUTO or MANUAL
    private java.math.BigDecimal weightmentQuantityKg;
    private String uomNetWeight;  // e.g., KGS
    private Integer weightmentQuantityNo;
    private String uomNo;  // e.g., PCS, NOS

}
