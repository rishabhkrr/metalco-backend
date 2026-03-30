package com.indona.invento.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "blocked_product")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlockedProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String itemDescription;
    private BigDecimal availableQuantityKg;

    @ManyToOne
    @JoinColumn(name = "blocked_quantity_id")
    @JsonBackReference
    private BlockedQuantityEntity blockedQuantity;
}
