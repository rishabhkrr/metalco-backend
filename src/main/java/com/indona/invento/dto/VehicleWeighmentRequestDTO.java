package com.indona.invento.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VehicleWeighmentRequestDTO {

    private String weightmentRefNumber;
    private String unit;//
    private String vehicleNumber;//
    private String purpose;//
    private String mode;//
    private String invoiceNumber;//
    private List<String> medcOrDcNumbers;
    private List<String> poNumbers;
    private List<String> medciNumbers;
    private String dcNumber;//
    private String medcpNumber;//
    private String gateEntryRefNo;//
    private String vehiclePhotoWithLoad;
    private String vehiclePhotoEmpty;
    private Double loadWeight;//
    private Double emptyWeight;//
    private Boolean verified;
    private String userId;
}
