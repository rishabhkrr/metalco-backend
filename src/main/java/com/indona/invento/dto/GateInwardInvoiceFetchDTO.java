package com.indona.invento.dto;

import java.util.List;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GateInwardInvoiceFetchDTO {

    private String gatePassRefNumber;
    private String medcNumber;
    private String dcNumber;
    private String eWayBillNumber;
    private String vehicleNumber;

    private List<String> invoiceScanUrls;
    private List<String> dcDocumentScanUrls;
    private List<String> eWayBillScanUrls;
    private List<String> vehicleDocumentsScanUrls;

    // From DeliveryChallanJW
    private String unit;
    private String subContractorCode;
    private String subContractorName;

    // From Vehicle Weighment
    private String weightmentRefNumber;
    private Double loadWeight;
    private Double emptyWeight;
}

