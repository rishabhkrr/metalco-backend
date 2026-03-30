package com.indona.invento.entities;

import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "process_entry")
public class ProcessEntryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String processType;
    private String operationType;
    private String packingType;
    private String packingStyle;
    private String mode;

    private String additionalProcesses;
}
