package com.indona.invento.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SoSummaryDTO {
    private String userId;
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss", timezone = "Asia/Kolkata")
    private LocalDateTime timestamp;
    private String quotationNo;
    private String soNumber;
    private String unit;
    private String customerPoNo;
    private String customerCode;
    private String customerName;
    private String customerPhoneNo;
    private String customerEmail;
    private String marketingExecutiveName;
    private String managementAuthority;
    private Boolean packingStatus;

    private List<SoSummaryItemDTO> items;
}
