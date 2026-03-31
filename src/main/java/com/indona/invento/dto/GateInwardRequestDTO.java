package com.indona.invento.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GateInwardRequestDTO {
    private String unitCode;
    private String medcNumber;
    private String mode;
    private String purpose;
    private String invoiceNumber;
    private String vehicleNumber;
    private String medciNumber;
    private String driverName;
    private List<String> poNumbers;
    private String dcNumber;
    private String eWayBillNumber;
    private List<TCDetailsDTO> testCertificates;
    private String vehicleWeighmentStatus;
    private String materialUnloadingStatus;
    private String vehicleOutStatus;
    private List<String> medciScanUrls;
    private List<String> medcScanUrls;
    private List<String> invoiceScanUrls;
    private List<String> dcDocumentScanUrls;
    private List<String> testCertificateScanUrls;
    private List<String> eWayBillScanUrls;
    private List<String> vehicleDocumentsScanUrls;
    private List<String> vehicleWeighmentScanUrls;
    private List<String> materialUnloadingStatusUrls;
}

