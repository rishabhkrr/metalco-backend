    package com.indona.invento.entities;

    import com.fasterxml.jackson.annotation.JsonBackReference;
    import jakarta.persistence.*;
    import lombok.AllArgsConstructor;
    import lombok.Builder;
    import lombok.Data;
    import lombok.NoArgsConstructor;


    @Entity
    @Table(name = "po_generation_item")
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public class POGenerationItemEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String prNumber;
        private String prCreatedBy;
        private String unit;
        private String deliveryAddress;
        private String sectionNo;
        private String itemDescription;
        private String productCategory;
        private String brand;
        private String grade;
        private String temper;
        private Double requiredQuantity;
        private String uom;
        private String prTypeAndReasonVerifiaction;
        private String rmReceiptStatus;
        private String soLineNumber;
        private Double receivedNetWeight;

        private String orderType;




        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "po_generation_id")
        @JsonBackReference
        private POGenerationEntity poGeneration;
    }
