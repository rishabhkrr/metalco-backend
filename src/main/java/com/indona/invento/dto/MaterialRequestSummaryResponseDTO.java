package com.indona.invento.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MaterialRequestSummaryResponseDTO {
    private Long id;
    private String mrNumber;
    private String unitCode;
    private String unitName;
    private String requestingUnit;
    private String deliveryAddress;
    private String status;
    private String requestingUnitUnitCode;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm")
    private LocalDateTime timestamp;

    private List<MaterialRequestSummaryItemDTO> items;



}
