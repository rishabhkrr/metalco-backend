package com.indona.invento.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesmanIncentiveDto {

    private String materialGradeAndTemper;
    private Double ratePerKg;
    private Double lapseInterestRate;
    private LocalDate effectiveDate;
}

