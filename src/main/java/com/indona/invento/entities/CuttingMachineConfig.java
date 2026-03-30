package com.indona.invento.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cutting_machine_config")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CuttingMachineConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer idealBladeSpeed;
    private Integer idealCuttingFeed;
    private Integer maxCuttingThickness;
    private Integer maxCuttingLength;
    private Integer minCuttingThickness;
    private Integer minCuttingLength;

    @OneToOne
    @JoinColumn(name = "machine_id")
    @JsonIgnore
    private MachineMasterEntity machine;
}

