package com.indona.invento.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GateEntryPackingAndDispatchRequestDTO {

    private String unitPackingAndDispatch;
    private String purposePackingAndDispatch;
    private String modePackingAndDispatch;
    private String medcScan;
    private String medciScan;
    private String medcpScan;
    private String invoiceNumberPackingAndDispatch;
    private String medcNumberPackingAndDispatch;
    private String medciNumberPackingAndDispatch;
    private String ewayBillNumberPackingAndDispatch;
    private String vehicleNumberPackingAndDispatch;
    private String driverNamePackingAndDispatch;

    private String medcpNumberPackingAndDispatch;
    private String invoiceScanPackingAndDispatch;
    private String vehicleDocumentsScanPackingAndDispatch;

    private String vehicleWeighmentStatusPackingAndDispatch;

}
