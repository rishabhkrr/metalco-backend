package com.indona.invento.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

@Data
public class DeliveryChallanMergedDTO {
	 private String medcNumber;

    // From DeliveryChallanJWEntity
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss", timezone = "Asia/Kolkata")
    private LocalDateTime timestamp;
    private String unit;
    private String subContractorCode;
    private String subContractorName;

    // From GateInwardEntity
    private String gatePassRefNumber;
    private String eWayBillNumber;
    private String vehicleNumber;
    private List<String> invoiceScanUrls;
    private List<String> dcDocumentScanUrls;
    private List<String> eWayBillScanUrls;
    private List<String> vehicleDocumentsScanUrls;

    // From VehicleWeighmentEntity
    private String weightmentRefNumber;
    private Double loadWeight;
    private Double emptyWeight;
}