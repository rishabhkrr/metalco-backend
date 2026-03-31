package com.indona.invento.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class VehicleWeighmentResponseDTO {

    private Long id;
    private String weightmentRefNumber;
    private String unit;
    private String vehicleNumber;
    private String invoiceNumber;
    private List<String> medcOrDcNumbers;
    private List<String> poNumbers;
    private List<String> medciNumbers;
    private String dcNumber;
    private String medcpNumber;
    private String purpose;
    private String gateEntryRefNo;
    private String vehiclePhotoWithLoad;
    private String vehiclePhotoEmpty;
    private Double loadWeight;
    private Double emptyWeight;
    private Boolean verified;
    private String userId;
    private String mode;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm", timezone = "Asia/Kolkata")
    private Date createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm", timezone = "Asia/Kolkata")
    private Date updatedAt;
}
