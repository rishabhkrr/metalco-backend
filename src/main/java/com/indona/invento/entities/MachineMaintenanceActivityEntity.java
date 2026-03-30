package com.indona.invento.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "machine_maintenance_activity")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MachineMaintenanceActivityEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String machineName;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Long breakdownMinutes;

    private String breakdownReason;

    private String remarks;

    private String status;  // BREAKDOWN / RUNNING / DONE
}
