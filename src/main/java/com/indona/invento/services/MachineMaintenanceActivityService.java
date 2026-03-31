package com.indona.invento.services;

import com.indona.invento.dto.MachineMaintenanceFilterDto;
import com.indona.invento.entities.MachineMaintenanceActivityEntity;

import java.time.LocalDate;
import java.util.List;

public interface MachineMaintenanceActivityService {

    MachineMaintenanceActivityEntity startBreakdown(String machineName, String reason, String remarks);

    MachineMaintenanceActivityEntity stopBreakdown(Long id, String reason, String remarks);

    List<MachineMaintenanceFilterDto> filter(String machineName, String reason, String status, LocalDate fromStartDate, LocalDate toStartDate);
}
