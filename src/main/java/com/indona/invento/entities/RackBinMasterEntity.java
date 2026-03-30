package com.indona.invento.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "rack_bin_master")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RackBinMasterEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String unitCode;   // Directly store as string
    private String unitName;   // Also store unit name directly

    private String storageType;
    private String storageArea;
    private String rackNo;
    private String columnNo;
    private String binNo;
    private String status;
    private String binCapacity;
    private Double distance;
    private String itemCategory;
    private Double currentStorage;
    private String qr;
    private Integer storageAreaOrder;
    private boolean automated;

}
