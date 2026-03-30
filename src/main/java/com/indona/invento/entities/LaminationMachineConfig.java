package com.indona.invento.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "lamination_machine_config")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LaminationMachineConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer idealRollerSpeed;
    private Integer conveyorFeed;
    private Integer maxSheetThickness;
    private Integer minSheetThickness;

    @OneToOne
    @JoinColumn(name = "machine_id")
    @JsonIgnore
    private MachineMasterEntity machine;
}

