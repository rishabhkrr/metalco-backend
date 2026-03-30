    package com.indona.invento.entities;

    import com.fasterxml.jackson.annotation.JsonManagedReference;
    import jakarta.persistence.*;
    import lombok.AllArgsConstructor;
    import lombok.Builder;
    import lombok.Data;
    import lombok.NoArgsConstructor;


    import java.util.Date;
    import java.util.List;
    import java.time.LocalDate;

    @Entity
    @Table(name = "po_generation")
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public class POGenerationEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String supplierCode;
        private String supplierName;
        private Date timeStamp;
        private String poNumber;
        private String poGeneratedBy;
        private String billingAddress;
        private String shippingAddress;
        private String poStatus;
        private String remarks;
        private String pdflink;

        private String dispatchThrough;
        private String otherReference;
        private String unit;
        private String rmReceiptStatus;   // OVERALL STATUS

        private LocalDate poValidity;   // PO Validity Date - stored as array [year, month, day]

        @ElementCollection
        private List<String> termsOfDelivery; // ✅ NEW

        @OneToMany(mappedBy = "poGeneration", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
        @JsonManagedReference
        private List<POGenerationItemEntity> items;

        @PrePersist
        protected void onCreate() {
            this.timeStamp = new Date();
        }

    }
