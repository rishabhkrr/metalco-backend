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
public class GRNRequestDTO {

    private String unit;
    private String invoiceNumber;
    private List<String> poNumber;
    private String vehicleNumber;

    private String invoiceDocument;
    private String testCertificateDocument;
    private String ewayBillDocument;
    private String vehicleDocuments;

    private Double vehicleLoadWeightKg;
    private Double vehicleEmptyWeightKg;
    private String weighmentRefNumber;
    private String GateEntryRefNo;
    private String MedcNumber;
    private String SupplierCode;
    private String SupplierName;
    private String EwayBillNumber;

    private String userId;
    private String mode;

    // ✅ New field — common for both "with PO" and "without PO"
    private List<GRNItemRequestDTO> items;

    // still keep itemsWithoutPO for backward compatibility (optional)
    private List<GRNItemRequestDTO> itemsWithoutPO;

    private boolean dispatch;

    private String status;

}
