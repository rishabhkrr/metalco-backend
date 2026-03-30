package com.indona.invento.services.impl;

import com.indona.invento.dao.SalesmanMasterRepository;
import com.indona.invento.dao.UserMasterRepository;
import com.indona.invento.dto.SalesmanIncentiveDto;
import com.indona.invento.dto.SalesmanMasterDto;
import com.indona.invento.entities.SalesmanIncentiveEntity;
import com.indona.invento.entities.SalesmanMasterEntity;
import com.indona.invento.entities.UserMasterEntity;
import com.indona.invento.services.SalesmanMasterService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SalesmanMasterServiceImpl implements SalesmanMasterService {

    @Autowired
    private SalesmanMasterRepository repository;

    @Autowired
    private UserMasterRepository userMasterRepository;


    @Override
    @Transactional
    public SalesmanMasterEntity createSalesman(SalesmanMasterDto dto) {
        log.info("🚀 Creating Salesman for userId={}", dto.getUserId());

        // 🔹 Build Salesman Entity
        SalesmanMasterEntity entity = SalesmanMasterEntity.builder()
                .unitCode(dto.getUnitCode())
                .unitName(dto.getUnitName())
                .userId(dto.getUserId())
                .userName(dto.getUserName())
                .department(dto.getDepartment())
                .designation(dto.getDesignation())
                .dateOfJoining(dto.getDateOfJoining())
                .userIdStatus(dto.getUserIdStatus())
                .modulesWithAccess(dto.getModulesWithAccess())
                .dateOfEnding(dto.getDateOfEnding())
                .status("Pending")
                .build();

        // 🔹 Build Incentive Entities
        List<SalesmanIncentiveEntity> incentiveEntities = new ArrayList<>();
        if (dto.getIncentiveRates() != null && !dto.getIncentiveRates().isEmpty()) {
            for (SalesmanIncentiveDto incDto : dto.getIncentiveRates()) {
                SalesmanIncentiveEntity incEntity = SalesmanIncentiveEntity.builder()
                        .materialGradeAndTemper(incDto.getMaterialGradeAndTemper())
                        .ratePerKg(incDto.getRatePerKg())
                        .lapseInterestRate(incDto.getLapseInterestRate())
                        .effectiveDate(incDto.getEffectiveDate())
                        .salesman(entity) // 🔗 Set parent reference
                        .build();
                incentiveEntities.add(incEntity);
            }
        }

        entity.setIncentiveRates(incentiveEntities);

        // 🔹 Save Salesman
        SalesmanMasterEntity savedSalesman = repository.save(entity);
        log.info("✅ Salesman saved with id={}", savedSalesman.getId());

        // 🔹 Update UserMaster -> set isSalesmanCreated = true
        userMasterRepository.findByUserId(dto.getUserId()).ifPresent(user -> {
            user.setIsSalesmanCreated(true);
            userMasterRepository.save(user);
            log.info("📌 UserMaster updated: userId={} isSalesmanCreated=true", user.getUserId());
        });

        return savedSalesman;
    }

    @Transactional
    @Override
    public SalesmanMasterEntity update(Long id, SalesmanMasterDto dto) {
        SalesmanMasterEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Salesman not found with ID: " + id));

        // 🔄 Update basic fields
        entity.setUnitCode(dto.getUnitCode());
        entity.setUnitName(dto.getUnitName());
        entity.setUserId(dto.getUserId());
        entity.setUserName(dto.getUserName());
        entity.setDepartment(dto.getDepartment());
        entity.setDesignation(dto.getDesignation());
        entity.setDateOfJoining(dto.getDateOfJoining());
        entity.setUserIdStatus(dto.getUserIdStatus());
        entity.setStatus("PENDING");
        entity.setDateOfEnding(dto.getDateOfEnding());
        entity.setModulesWithAccess(dto.getModulesWithAccess());

        // 🧹 Clear + rebuild IncentiveRates list
        entity.getIncentiveRates().clear();
        List<SalesmanIncentiveEntity> updatedIncentives = dto.getIncentiveRates().stream().map(incDto -> {
            SalesmanIncentiveEntity incEntity = new SalesmanIncentiveEntity();
            incEntity.setMaterialGradeAndTemper(incDto.getMaterialGradeAndTemper());
            incEntity.setRatePerKg(incDto.getRatePerKg());
            incEntity.setLapseInterestRate(incDto.getLapseInterestRate());
            incEntity.setEffectiveDate(incDto.getEffectiveDate());
            incEntity.setSalesman(entity); // 👈 back-reference set
            return incEntity;
        }).collect(Collectors.toList());
        entity.getIncentiveRates().addAll(updatedIncentives);

        return repository.save(entity);
    }



    @Override
    public Page<SalesmanMasterEntity> getAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    public SalesmanMasterEntity getById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        if (repository.existsById(id)) {
            // Step 1: Get salesman entity safely
            repository.findById(id).ifPresent(salesman -> {
                // Step 2: Find linked user safely
                userMasterRepository.findByUserId(salesman.getUserId())
                        .ifPresent(user -> {
                            user.setIsSalesmanCreated(false); // ✅ set flag to false
                            userMasterRepository.save(user);
                            log.info("🔄 Updated User {} isSalesmanCreated=false", user.getUserName());
                        });
            });

            // Step 3: Delete salesman
            repository.deleteById(id);
            log.info("🗑️ Deleted SalesmanMaster with id={}", id);
            return true;
        }
        return false;
    }


    @Override
    public List<SalesmanMasterEntity> getAllWithoutPagination() {
        return repository.findAll();
    }

    @Override
    public SalesmanMasterEntity approveSalesman(Long id) throws Exception {
        SalesmanMasterEntity salesman = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Salesman not found with ID: " + id));
        salesman.setStatus("APPROVED");
        return repository.save(salesman);
    }

    @Override
    public SalesmanMasterEntity rejectSalesman(Long id) throws Exception {
        SalesmanMasterEntity salesman = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Salesman not found with ID: " + id));
        salesman.setStatus("REJECTED");
        return repository.save(salesman);
    }

}

