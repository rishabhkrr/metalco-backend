package com.indona.invento.dto;

import jakarta.validation.constraints.NegativeOrZero;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GradeDto {
    private String gradeValue;
}
