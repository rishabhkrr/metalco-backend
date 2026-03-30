package com.indona.invento.services.impl;

import com.indona.invento.dao.UnitMasterRepository;
import com.indona.invento.dao.UserMasterRepository;
import com.indona.invento.dto.UserMasterDto;
import com.indona.invento.entities.SubModuleAccessEntity;
import com.indona.invento.entities.UserMasterEntity;
import com.indona.invento.services.UserMasterService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserMasterServiceImpl implements UserMasterService {

    @Autowired
    private UserMasterRepository repository;

    @Autowired
    private UnitMasterRepository unitRepo;

    private String generateUserId() {
        long count = repository.count() + 1;
        return "USR" + String.format("%04d", count);
    }

    @Override
    public UserMasterEntity create(UserMasterDto dto) {
        UserMasterEntity user = new UserMasterEntity();
        user.setUnitCode(dto.getUnitCode());
        user.setUnitName(dto.getUnitName());
        user.setUserId(generateUserId());
        user.setUserName(dto.getUserName());
        user.setPassword(dto.getPassword());
        user.setDepartment(dto.getDepartment());
        user.setDesignation(dto.getDesignation());
        user.setModulesWithAccess(dto.getModulesWithAccess());
        user.setStatus("INACTIVE");
        user.setDateOfJoining(dto.getDateOfJoining());
        user.setDateOfEnding(dto.getDateOfEnding());

        List<SubModuleAccessEntity> subModules = dto.getSubModulesWithAccess().stream().map(subDto -> {
            SubModuleAccessEntity subEntity = new SubModuleAccessEntity();
            subEntity.setSubModuleName(subDto.getSubModuleName());
            subEntity.setReadAccess(subDto.isReadAccess());
            subEntity.setCreateAccess(subDto.isCreateAccess());
            subEntity.setEditAccess(subDto.isEditAccess());
            subEntity.setDeleteAccess(subDto.isDeleteAccess());
            subEntity.setApproveAccess(subDto.isApproveAccess());
            subEntity.setUser(user);  // 👈 Link back to parent
            return subEntity;
        }).collect(Collectors.toList());


        user.setSubModulesWithAccess(subModules);

        return repository.save(user);
    }


    @Transactional
    @Override
    public UserMasterEntity update(Long id, UserMasterDto dto) {
        UserMasterEntity user = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Clear old submodules safely
        List<SubModuleAccessEntity> existingSubModules = user.getSubModulesWithAccess();
        if (existingSubModules != null) {
            existingSubModules.clear(); // triggers orphan removal
        }

        // Create new submodules
        List<SubModuleAccessEntity> newSubModules = dto.getSubModulesWithAccess().stream().map(subDto -> {
            SubModuleAccessEntity subEntity = new SubModuleAccessEntity();
            subEntity.setSubModuleName(subDto.getSubModuleName());
            subEntity.setReadAccess(subDto.isReadAccess());
            subEntity.setCreateAccess(subDto.isCreateAccess());
            subEntity.setEditAccess(subDto.isEditAccess());
            subEntity.setApproveAccess(subDto.isApproveAccess());
            subEntity.setDeleteAccess(subDto.isDeleteAccess());
            subEntity.setUser(user); // link to parent
            return subEntity;
        }).toList();


        // Add new submodules to same list reference
        user.getSubModulesWithAccess().addAll(newSubModules);

        // Update other fields
        user.setUnitCode(dto.getUnitCode());
        user.setUnitName(dto.getUnitName());
        user.setUserName(dto.getUserName());
        user.setPassword(dto.getPassword());
        user.setDepartment(dto.getDepartment());
        user.setDesignation(dto.getDesignation());
        user.setModulesWithAccess(dto.getModulesWithAccess());
        user.setStatus(dto.getStatus());
        user.setDateOfJoining(dto.getDateOfJoining());
        user.setDateOfEnding(dto.getDateOfEnding());

        return repository.save(user);
    }




    @Override
    public Page<UserMasterEntity> getAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    public UserMasterEntity getById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public UserMasterEntity delete(Long id) {
        Optional<UserMasterEntity> optionalUser = repository.findById(id);
        if (optionalUser.isPresent()) {
            UserMasterEntity user = optionalUser.get();
            repository.delete(user);
            return user; // Return deleted entity
        }
        return null;
    }


    @Override
    public List<UserMasterEntity> getAllWithoutPagination() {
        return repository.findAll();
    }

    @Override
    public UserMasterEntity getByUserName(String userName) {
        return repository.findByUserName(userName);
    }

    @Override
    public UserMasterEntity approveUser(Long id) throws Exception {
        UserMasterEntity user = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));
        user.setStatus("APPROVED");
        return repository.save(user);
    }

    @Override
    public UserMasterEntity rejectUser(Long id) throws Exception {
        UserMasterEntity user = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));
        user.setStatus("REJECTED");
        return repository.save(user);
    }

}
