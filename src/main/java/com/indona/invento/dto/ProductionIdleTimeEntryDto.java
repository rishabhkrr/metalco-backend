package com.indona.invento.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductionIdleTimeEntryDto {

    private Long id;

    private String machineName;
    private String startTime;
    private String endTime;

    private Integer idleMinutes;
    private String idleReason;
    private String remarks;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Kolkata")
    private Instant timestamp;
}
