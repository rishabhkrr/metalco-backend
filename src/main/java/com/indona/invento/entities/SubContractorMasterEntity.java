package com.indona.invento.entities;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "sub_contractor_master")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubContractorMasterEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String gstRegistrationType;
    private String supplierCategory;
    private String subContractorType;

    @Column(unique = true, nullable = false)
    private String subContractorCode;

    private String subContractorName;
    private String mailingBillingName;
    private String supplierNickname;
    private Boolean multipleAddresses;

    private String gstOrUin;
    private String gstStateCode;
    private String pan;
    private String isTanAvailable;
    private String tanNumber;
    private String isUdyamAvailable;
    private String udyamNumber;
    private String isIecAvailable;
    private String iecCode;

    private String gstCertificatePath;
    private String otherDocumentsPath;

    private String interestCalculation;
    private Double rateOfInterest;

    private String typeOfEntity;         // Type of entity

    // New fields for sub-contractor relationship and file uploads
    private String relatedStatus;    // Related / Non-Related
    private String panFileUpload;    // Pan file upload link/path
    private String udyamFileUpload;  // Udyam file upload link/path
    private String status;           // Pending / APPROVED / REJECTED (default: Pending)

    @OneToMany(mappedBy = "subContractor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SubContractorAddressEntity> addressDetails;

    @OneToMany(mappedBy = "subContractor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SubContractorContactEntity> contactDetails;

    @OneToMany(mappedBy = "subContractor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SubContractorBankEntity> bankDetails;
}
