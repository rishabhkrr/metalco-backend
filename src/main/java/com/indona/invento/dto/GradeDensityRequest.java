package com.indona.invento.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class GradeDensityRequest {
    private Integer grade;
    private BigDecimal density;
}

