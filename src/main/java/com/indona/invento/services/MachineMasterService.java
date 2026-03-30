package com.indona.invento.services;

import com.indona.invento.dto.MachineMasterDto;
import com.indona.invento.entities.MachineMasterEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MachineMasterService {

    MachineMasterEntity createMachine(MachineMasterDto dto);

    MachineMasterEntity getMachineById(Long id);

    Page<MachineMasterEntity> getAllMachines(Pageable pageable);

    MachineMasterEntity deleteMachine(Long id);


    MachineMasterEntity updateMachine(Long id, MachineMasterDto dto);

    List<MachineMasterEntity> getAllMachinesWithoutPagination();

    List<MachineMasterEntity> getMachinesByUnitCode(String unitCode);
    void deleteAllMachines();
    MachineMasterEntity approveMachine(Long id) throws Exception;
    MachineMasterEntity rejectMachine(Long id) throws Exception;

    /**
     * Get all APPROVED machine names only
     */
    List<String> getApprovedMachineNames();

    /**
     * Get machine cutting config (idealBladeSpeed, idealCuttingFeed) by machine name
     */
    java.util.Map<String, Object> getMachineCuttingConfig(String machineName);

}
