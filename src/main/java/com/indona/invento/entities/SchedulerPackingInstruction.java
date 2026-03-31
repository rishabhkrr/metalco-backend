package com.indona.invento.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "scheduler_packing_instruction")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SchedulerPackingInstruction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String typeOfPacking;
    private String weightInstructions;
    private String additionalRemarks;

    @OneToOne
    @JoinColumn(name = "scheduler_id")
    @JsonIgnore
    private SalesOrderSchedulerEntity scheduler;
}
