package com.indona.invento.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "production_idle_time_entry")
public class ProductionIdleTimeEntryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String machineName;

    private String startTime;

    private String endTime;

    private Integer idleMinutes;
    private String idleReason;
    private String remarks;

    @Column(updatable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Kolkata")
    private Instant timestamp;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "production_entry_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ProductionEntryEntity productionEntry;

    @PrePersist
    protected void onCreate() {
        this.timestamp = Instant.now();
    }
}
