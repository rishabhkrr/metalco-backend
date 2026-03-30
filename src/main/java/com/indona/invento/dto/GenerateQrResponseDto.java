package com.indona.invento.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenerateQrResponseDto {

    private Long lineItemId;
    private String qrCode;
    private String qrCodeImageUrl;
    private Boolean success;
    private String message;
}
