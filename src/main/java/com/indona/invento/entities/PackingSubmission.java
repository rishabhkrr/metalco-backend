package com.indona.invento.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "packing_submission")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PackingSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String soNumber;
    private String lineNumber;

    private String unit;
    private String customerCode;
    private String customerName;

    private String orderType;
    private String productCategory;
    private String itemDescription;
    private String brand;
    private String grade;
    private String temper;
    private String dimension;

    private BigDecimal quantityKg;
    private String uomKg;

    private Integer quantityNo;
    private String uomNo;

    private String packingInstructions; // Packing Type
    private String packingStatus;       // PENDING / COMPLETE
    private String pdf;

    @Column(name = "packing_id")
    private String packingId;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "packingSubmission", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    @Builder.Default
    private List<PackingBatchDetail> batchDetails = new ArrayList<>();

    // Helper method to add batch detail
    public void addBatchDetail(PackingBatchDetail batchDetail) {
        batchDetails.add(batchDetail);
        batchDetail.setPackingSubmission(this);
    }

    // Helper method to remove batch detail
    public void removeBatchDetail(PackingBatchDetail batchDetail) {
        batchDetails.remove(batchDetail);
        batchDetail.setPackingSubmission(null);
    }
}
