package com.indona.invento.entities;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "supplier_master")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierMasterEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String gstRegistrationType;   // 🔹 Added from dropdown
    private String supplierCategory;      // 🔹 Added (A/B/C)
    private String supplierType;          // Local / Standard

    @Column(unique = true, nullable = false)
     String supplierCode;          // Auto-generated (e.g. MESU0001)

    private String supplierName;          // UPPER
    private String mailingBillingName;    // 🔹 Added
    private String supplierNickname;      // 🔹 Added
    private Boolean multipleAddress;      // 🔹 Added (Yes/No dropdown)

    private String gstOrUin;
    private String gstStateCode;
    // 15 characters
    private String pan;
    private String isTanAvailable;// 10 chars (5 letters + 4 digits + 1 letter)
    private String tanNumber;
    private String isUdyamAvailable;// 🔹 Added (conditionally required)
    private String udyamNumber;
    private String isIecAvailable;// 🔹 Added
    private String iecCode;              // 🔹 Added

    private String gstCertificatePath;   // optional file
    private String otherDocumentsPath;   // 🔹 Added (n documents zipped path or folder)

    private String interestCalculation; // Yes/No dropdown
    private Double rateOfInterest;       // % annual
    private String brand;                // 🔹 Added (Dropdown)

    private String typeOfEntity;         // Type of entity

    // New fields for supplier relationship and file uploads
    private String relatedStatus;    // Related / Non-Related
    private String panFileUpload;    // Pan file upload link/path
    private String udyamFileUpload;  // Udyam file upload link/path
    private String status;           // Pending / Active / Inactive (default: Pending)


    @OneToMany(mappedBy = "supplier", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AddressDetailsEntity> addressDetails;

    @OneToMany(mappedBy = "supplier", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ContactDetailsEntity> contactDetails;

    @OneToMany(mappedBy = "supplier", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BankDetailsEntity> bankDetails;

}

