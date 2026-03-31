package com.indona.invento.services.impl;

import com.indona.invento.dao.MachineMaintenanceActivityRepository;
import com.indona.invento.dto.MachineMaintenanceFilterDto;
import com.indona.invento.entities.MachineMaintenanceActivityEntity;
import com.indona.invento.services.MachineMaintenanceActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MachineMaintenanceActivityServiceImpl implements MachineMaintenanceActivityService {

    private final MachineMaintenanceActivityRepository repo;

    @Override
    public MachineMaintenanceActivityEntity startBreakdown(String machineName, String reason, String remarks) {

        MachineMaintenanceActivityEntity entry = MachineMaintenanceActivityEntity.builder()
                .machineName(machineName)
                .startTime(LocalDateTime.now())
                .breakdownReason(reason)
                .remarks(remarks)
                .status("BREAKDOWN")
                .build();

        return repo.save(entry);
    }

    @Override
    public MachineMaintenanceActivityEntity stopBreakdown(Long id, String reason, String remarks) {
        MachineMaintenanceActivityEntity entry = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Record not found"));

        // Set End time
        LocalDateTime end = LocalDateTime.now();
        entry.setEndTime(end);

        // Set Breakdown reason & remarks
        entry.setBreakdownReason(reason);
        entry.setRemarks(remarks);

        // Calculate minutes between start and end
        long minutes = Duration.between(entry.getStartTime(), end).toMinutes();
        entry.setBreakdownMinutes(minutes);

        // Update status
        entry.setStatus("READY_TO_RUN");

        return repo.save(entry);
    }

    @Override
    public List<MachineMaintenanceFilterDto> filter(String machineName, String reason, String status,
                                                    LocalDate fromStartDate, LocalDate toStartDate) {

        if (fromStartDate == null) fromStartDate = LocalDate.now().minusMonths(1);
        if (toStartDate == null) toStartDate = LocalDate.now();

        LocalDateTime from = fromStartDate.atStartOfDay();
        LocalDateTime to = toStartDate.atTime(23, 59, 59);

        List<MachineMaintenanceActivityEntity> result =
                repo.filter(
                        machineName != null && !machineName.isBlank() ? machineName : null,
                        reason != null && !reason.isBlank() ? reason : null,
                        status != null && !status.isBlank() ? status : null,
                        from, to
                );
        result.sort((a, b) -> b.getStartTime().compareTo(a.getStartTime()));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        return result.stream().map(e -> MachineMaintenanceFilterDto.builder()
                .id(e.getId())
                .machineName(e.getMachineName())
                .startTime(e.getStartTime() != null ? e.getStartTime().format(formatter) : "")
                .endTime(e.getEndTime() != null ? e.getEndTime().format(formatter) : "")
                .breakdownMinutes(e.getBreakdownMinutes())
                .breakdownReason(e.getBreakdownReason())
                .remarks(e.getRemarks())
                .status(e.getStatus())
                .build()
        ).toList();
    }

}
