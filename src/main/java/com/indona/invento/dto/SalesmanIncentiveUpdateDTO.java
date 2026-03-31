package com.indona.invento.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesmanIncentiveUpdateDTO {
    private Long incentiveId;
    private BigDecimal amountReceived;
    private LocalDate dateOfPayment;

}
