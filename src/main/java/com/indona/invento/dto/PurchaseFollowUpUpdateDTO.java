package com.indona.invento.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseFollowUpUpdateDTO {
    private String poNumber;
    private String salesOrderNumber;
}

