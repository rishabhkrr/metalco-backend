package com.indona.invento.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "so_update_packing_instruction")
public class SoUpdatePackingInstruction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String typeOfPacking;
    private String weightInstructions;
    private String additionalRemarks;

    @OneToOne
    @JoinColumn(name = "so_update_id")
    @JsonIgnore
    private SoUpdate soUpdate;
}
