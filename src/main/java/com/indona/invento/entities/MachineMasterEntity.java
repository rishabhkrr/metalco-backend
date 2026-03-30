package com.indona.invento.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "machine_master")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MachineMasterEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String unitCode;         // From Unit Master
    private String unitName;         // Auto from Unit Master
    private String machineId;        // Manual
    private String machineName;      // Manual
    private String machineType;      // Manual
    private String modelNumber;      // Manual
    private String manufacturer;     // Manual
    private String machineSpecifications; // Manual Click
    private String status;           // Pending / APPROVED / REJECTED (default: Pending)

    @OneToOne(mappedBy = "machine", cascade = CascadeType.ALL)
    private CuttingMachineConfig cuttingConfig;

    @OneToOne(mappedBy = "machine", cascade = CascadeType.ALL)
    private LaminationMachineConfig laminationConfig;
}
