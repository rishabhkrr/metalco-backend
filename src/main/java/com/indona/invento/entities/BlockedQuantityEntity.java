package com.indona.invento.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Entity
@Table(name = "blocked_quantity")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlockedQuantityEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String quotationNo;
    private String customerName;
    private String marketingExecutiveName;
    private String pdfLink;

    @OneToMany(mappedBy = "blockedQuantity", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<BlockedProductEntity> products;

    private Date createdAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = new Date();
    }
}
