package com.indona.invento.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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
public class CustomerOverdueResponseDTO {

    private Integer creditDays;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate billDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;

    private BigDecimal dueAmount;
}

