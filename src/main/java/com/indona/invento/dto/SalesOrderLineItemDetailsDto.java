package com.indona.invento.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesOrderLineItemDetailsDto {
    private String soNumber;
    private String lineNumber;
    private String itemDescription;
    private Double quantityKg;
    private LocalDate targetDispatchDate;
}

