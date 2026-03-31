package com.indona.invento.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "new_customer")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewCustomerDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String gstRegistrationType;
    private String gstOrUin;         // GST/UIN or UNREGISTERED
    private String customerCategory;                    // Yes/No from dropdown

  //  @Column(unique = true, nullable = false)
    private String customerCode;             // Auto-generated code like MECU0001

    private String customerName;             // Uppercase manual
    private String mailingBillingName;       // Manual
    private String customerNickname;         // Manual
    private Boolean multipleAddress;

    private Double creditLimitAmount;
    private Integer creditLimitDays;
    private Integer lockPeriodDays;

    private String pan;
    private String isTanAvailable;
    private String tanNumber;
    private String isUdyamAvailable;
    private String udyamNumber;
    private String isIecAvailable;
    private String iecCode;
    private String gstStateCode;


    private String gstCertificateLink;       // manual file link
    private String typeOfIndustry;           // Dropdown: Manufacturer/Trader
    private String applicationOfIndustry;    // Dropdown: Aerospace/Defence
    private String interestCalculation;     // Yes/No
    private Double rateOfInterest;           // % per annum
    private String otherDocuments;
    @Column(name = "status")
    private String status;
    private String customerBased;
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;



    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NewCustomerAddessEntity> addressDetails;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NewCustomerContactEntity> contactDetails;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NewCustomerBankDetailsEntity> bankDetails;

}
