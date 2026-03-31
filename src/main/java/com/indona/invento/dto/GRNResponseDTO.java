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
public class GRNResponseDTO {
    private Long id;
    private String timeStamp;
    private String grnRefNumber;
    private String unit;
    private String invoiceNumber;
    private List<String> poNumber;
    private String gateEntryRefNo;
    private List<String> testCertificateNumbers;
    private String ewayBillNumber;
    private String vehicleNumber;
    private String invoiceDocument;
    private String testCertificateDocument;
    private String ewayBillDocument;
    private String vehicleDocuments;
    private String supplierName;
    private String supplierCode;
    private Double vehicleLoadWeightKg;
    private Double vehicleEmptyWeightKg;
    private Double weighmentQuantity;
    private String materialUnloadingStatus;
    private String materialUnloadingNotes;
    private String createdAt;
    private String updatedAt;
    private String binStatus;
    private String weighmentRefNumber;
    private String medcNumber;
    private String requestingUnit;
    private String mrNumber;
    private String mode;
    private String requestedQty;
    private String materialType;
    private String requestingUnitCode;
    private List<TCDetailsDTO> testCertificates;

    private List<GRNItemResponseDTO> items;
    private List<MRWiseGRNItemsDTO> mrDetails;
    private String status;
}
