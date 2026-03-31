package com.indona.invento.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "packing_batch_detail")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PackingBatchDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String itemDescription;
    private String itemDimensions;
    private String batchNumber;
    private String dateOfInward;
    private BigDecimal qtyKg;
    private Integer qtyNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "packing_submission_id", nullable = false)
    @JsonBackReference
    private PackingSubmission packingSubmission;
}

