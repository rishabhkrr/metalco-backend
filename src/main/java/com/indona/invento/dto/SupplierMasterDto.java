package com.indona.invento.dto;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class
SupplierMasterDto {
    private String gstRegistrationType;
    private String supplierCategory;
    private String supplierType;

    private String supplierCode;

    private String supplierName;
    private String mailingBillingName;
    private String supplierNickname;
    private Boolean multipleAddress;

    private String gstOrUin;
    private String gstStateCode;
    private String pan;
    private String isTanAvailable;
    private String tanNumber;
    private String isUdyamAvailable;
    private String udyamNumber;
    private String isIecAvailable;
    private String iecCode;

    private String gstCertificatePath;     // ✅ typically for file path or uploaded link
    private String otherDocumentsPath;     // ✅ path to zip/folder or doc list

    private String interestCalculation;
    private Double rateOfInterest;
    private String brand;

    private String typeOfEntity;  // Type of entity (Supplier/Distributor/etc)

    // New fields for supplier relationship and file uploads
    private String relatedStatus;    // Related / Non-Related
    private String panFileUpload;    // Pan file upload link/path
    private String udyamFileUpload;  // Udyam file upload link/path
    private String status;           // Pending / Active / Inactive (default: Pending)

    private List<AddressDetailsDto> addressDetails;
    private List<ContactDetailsDto> contactDetails;
    private List<BankDetailsDto> bankDetails;


}
