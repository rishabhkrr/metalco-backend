package com.indona.invento.services.impl;

import com.indona.invento.dao.MachineMasterRepository;
import com.indona.invento.dto.MachineMasterDto;
import com.indona.invento.dto.CuttingMachineDto;
import com.indona.invento.dto.LaminationMachineDto;
import com.indona.invento.entities.MachineMasterEntity;
import com.indona.invento.entities.CuttingMachineConfig;
import com.indona.invento.entities.LaminationMachineConfig;
import com.indona.invento.services.MachineMasterService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MachineMasterServiceImpl implements MachineMasterService {

    private final MachineMasterRepository machineRepository;

    @Autowired
    public MachineMasterServiceImpl(MachineMasterRepository machineRepository) {
        this.machineRepository = machineRepository;
    }

    @Override
    public MachineMasterEntity createMachine(MachineMasterDto dto) {
        MachineMasterEntity machine = MachineMasterEntity.builder()
                .unitCode(dto.getUnitCode())
                .unitName(dto.getUnitName())
                .machineId(dto.getMachineId())
                .machineName(dto.getMachineName())
                .machineType(dto.getMachineType())
                .modelNumber(dto.getModelNumber())
                .manufacturer(dto.getManufacturer())
                .machineSpecifications(dto.getMachineSpecifications())
                .status("Pending")
                .build();

        // Cutting Configuration
        CuttingMachineDto cuttingDto = dto.getCuttingConfig();
        if (cuttingDto != null) {
            CuttingMachineConfig cuttingConfig = CuttingMachineConfig.builder()
                    .idealBladeSpeed(cuttingDto.getIdealBladeSpeed())
                    .idealCuttingFeed(cuttingDto.getIdealCuttingFeed())
                    .maxCuttingThickness(cuttingDto.getMaxCuttingThickness())
                    .maxCuttingLength(cuttingDto.getMaxCuttingLength())
                    .minCuttingThickness(cuttingDto.getMinCuttingThickness())
                    .minCuttingLength(cuttingDto.getMinCuttingLength())
                    .machine(machine)
                    .build();
            machine.setCuttingConfig(cuttingConfig);
        }

        // Lamination Configuration
        LaminationMachineDto laminationDto = dto.getLaminationConfig();
        if (laminationDto != null) {
            LaminationMachineConfig laminationConfig = LaminationMachineConfig.builder()
                    .idealRollerSpeed(laminationDto.getIdealRollerSpeed())
                    .conveyorFeed(laminationDto.getConveyorFeed())
                    .maxSheetThickness(laminationDto.getMaxSheetThickness())
                    .minSheetThickness(laminationDto.getMinSheetThickness())
                    .machine(machine)
                    .build();
            machine.setLaminationConfig(laminationConfig);
        }

        return machineRepository.save(machine);
    }

    @Override
    public MachineMasterEntity getMachineById(Long id) {
        return machineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Machine not found with ID: " + id));
    }

    @Override
    public Page<MachineMasterEntity> getAllMachines(Pageable pageable) {
        return machineRepository.findAll(pageable);
    }

    @Override
    public MachineMasterEntity deleteMachine(Long id) {
        MachineMasterEntity machine = machineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Machine not found with ID: " + id));

        machineRepository.delete(machine);
        return machine;
    }


    @Override
    public MachineMasterEntity updateMachine(Long id, MachineMasterDto dto) {
        MachineMasterEntity machine = machineRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Machine not found with this id: " + id ));

        // Update basic fields
        machine.setUnitCode(dto.getUnitCode());
        machine.setUnitName(dto.getUnitName());
        machine.setMachineId(dto.getMachineId());
        machine.setMachineName(dto.getMachineName());
        machine.setMachineType(dto.getMachineType());
        machine.setModelNumber(dto.getModelNumber());
        machine.setManufacturer(dto.getManufacturer());
        machine.setStatus("PENDING");
        machine.setMachineSpecifications(dto.getMachineSpecifications());

        // Cutting Config
        CuttingMachineDto cuttingDto = dto.getCuttingConfig();
        if (cuttingDto != null) {
            CuttingMachineConfig cuttingConfig = machine.getCuttingConfig();
            if (cuttingConfig == null) {
                cuttingConfig = new CuttingMachineConfig();
                cuttingConfig.setMachine(machine);
            }
            cuttingConfig.setIdealBladeSpeed(cuttingDto.getIdealBladeSpeed());
            cuttingConfig.setIdealCuttingFeed(cuttingDto.getIdealCuttingFeed());
            cuttingConfig.setMaxCuttingThickness(cuttingDto.getMaxCuttingThickness());
            cuttingConfig.setMaxCuttingLength(cuttingDto.getMaxCuttingLength());
            cuttingConfig.setMinCuttingThickness(cuttingDto.getMinCuttingThickness());
            cuttingConfig.setMinCuttingLength(cuttingDto.getMinCuttingLength());
            machine.setCuttingConfig(cuttingConfig);
        } else {
            machine.setCuttingConfig(null);
        }

        // Lamination Config
        LaminationMachineDto laminationDto = dto.getLaminationConfig();
        if (laminationDto != null) {
            LaminationMachineConfig laminationConfig = machine.getLaminationConfig();
            if (laminationConfig == null) {
                laminationConfig = new LaminationMachineConfig();
                laminationConfig.setMachine(machine);
            }
            laminationConfig.setIdealRollerSpeed(laminationDto.getIdealRollerSpeed());
            laminationConfig.setConveyorFeed(laminationDto.getConveyorFeed());
            laminationConfig.setMaxSheetThickness(laminationDto.getMaxSheetThickness());
            laminationConfig.setMinSheetThickness(laminationDto.getMinSheetThickness());
            machine.setLaminationConfig(laminationConfig);
        } else {
            machine.setLaminationConfig(null);
        }

        return machineRepository.save(machine);
    }

    @Override
    public List<MachineMasterEntity> getAllMachinesWithoutPagination() {
        return machineRepository.findAll();
    }

    @Override
    public List<MachineMasterEntity> getMachinesByUnitCode(String unitCode) {
        return machineRepository.findByUnitCode(unitCode);
    }


    @Override
    public void deleteAllMachines() {
        machineRepository.deleteAll();
    }

    @Override
    public MachineMasterEntity approveMachine(Long id) throws Exception {
        MachineMasterEntity machine = machineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Machine not found with ID: " + id));
        machine.setStatus("APPROVED");
        return machineRepository.save(machine);
    }

    @Override
    public MachineMasterEntity rejectMachine(Long id) throws Exception {
        MachineMasterEntity machine = machineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Machine not found with ID: " + id));
        machine.setStatus("REJECTED");
        return machineRepository.save(machine);
    }

    @Override
    public List<String> getApprovedMachineNames() {
        return machineRepository.findAll()
                .stream()
                .filter(machine -> "APPROVED".equalsIgnoreCase(machine.getStatus()))
                .map(MachineMasterEntity::getMachineName)
                .toList();
    }

    @Override
    public java.util.Map<String, Object> getMachineCuttingConfig(String machineName) {
        // Find machine by name
        MachineMasterEntity machine = machineRepository.findAll()
                .stream()
                .filter(m -> machineName.equalsIgnoreCase(m.getMachineName()))
                .findFirst()
                .orElse(null);

        if (machine == null) {
            return null;
        }

        // Get cutting config
        CuttingMachineConfig cuttingConfig = machine.getCuttingConfig();

        if (cuttingConfig == null) {
            return null;
        }

        // Return only idealBladeSpeed and idealCuttingFeed
        return java.util.Map.of(
                "idealBladeSpeed", cuttingConfig.getIdealBladeSpeed(),
                "idealCuttingFeed", cuttingConfig.getIdealCuttingFeed()
        );
    }
}
