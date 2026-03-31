package com.indona.invento.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "purchase_follow_up")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseFollowUpEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String salesOrderNumber;
    private String lineItemNumber;
    private String itemDescription;

    private String poNumber;
    private String supplierName;
    private String unit;
    private String poStatus;
    private Double requiredQuantity;

    private Date poOrderDate;

    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
