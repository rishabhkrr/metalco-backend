package com.indona.invento.dao;


import com.indona.invento.entities.MachineMasterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MachineMasterRepository extends JpaRepository<MachineMasterEntity, Long> {

    // Optional: Custom finder methods
    MachineMasterEntity findByMachineId(String machineId);

    List<MachineMasterEntity> findByUnitCode(String unitCode);

    List<MachineMasterEntity> findByMachineType(String machineType);

    @Query("SELECT DISTINCT m.machineName FROM MachineMasterEntity m WHERE m.machineName IS NOT NULL")
    List<String> findMachineNames();

    @Query("SELECT DISTINCT m.machineName FROM MachineMasterEntity m WHERE m.machineName IS NOT NULL AND m.status = 'APPROVED'")
    List<String> findApprovedMachineNames();

    MachineMasterEntity findByMachineName(String trim);
}

