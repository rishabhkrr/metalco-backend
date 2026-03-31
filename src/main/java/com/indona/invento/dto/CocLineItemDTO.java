package com.indona.invento.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CocLineItemDTO {

    private Long id;
    private String lineNumber;
    private String productCategory;
    private String itemDescription;
    private String brand;
    private String grade;
    private String temper;
    private String dimension;
    private String quantityKg;

    // CoC Generated Fields
    private String cocNumber;
    private LocalDateTime cocTimestamp;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

