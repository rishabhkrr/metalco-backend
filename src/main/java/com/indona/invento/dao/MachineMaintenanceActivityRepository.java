package com.indona.invento.dao;

import com.indona.invento.entities.MachineMaintenanceActivityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface MachineMaintenanceActivityRepository extends JpaRepository<MachineMaintenanceActivityEntity, Long> {

    @Query("""
       SELECT m FROM MachineMaintenanceActivityEntity m
       WHERE (:machineName IS NULL OR m.machineName = :machineName)
       AND (:reason IS NULL OR m.breakdownReason = :reason)
       AND (:status IS NULL OR m.status = :status)
       AND m.startTime BETWEEN :from AND :to
       ORDER BY m.startTime DESC
       """)
    List<MachineMaintenanceActivityEntity> filter(
            String machineName,
            String reason,
            String status,
            LocalDateTime from,
            LocalDateTime to
    );



    @Query("SELECT m.machineName FROM MachineMaintenanceActivityEntity m WHERE m.status = 'BREAKDOWN'")
    List<String> getMachinesUnderBreakdown();

    @Query("SELECT MIN(m.startTime) FROM MachineMaintenanceActivityEntity m")
    LocalDateTime findMinStartDate();


}
