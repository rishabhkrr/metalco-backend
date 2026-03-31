package com.indona.invento.services.impl;

import com.indona.invento.dao.ProcessEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.indona.invento.dto.ProcessEntryDto;
import com.indona.invento.entities.ProcessEntryEntity;
import com.indona.invento.services.ProcessEntryService;

import java.util.List;
import java.util.Optional;

@Service
public class ProcessEntryServiceImpl implements ProcessEntryService {

    @Autowired
    private ProcessEntryRepository repository;

    @Override
    public ProcessEntryEntity addEntry(ProcessEntryDto dto) {
        ProcessEntryEntity entity = ProcessEntryEntity.builder()
                .processType(dto.getProcessType())
                .operationType(dto.getOperationType())
                .packingType(dto.getPackingType())
                .packingStyle(dto.getPackingStyle())
                .mode(dto.getMode())
                .additionalProcesses(dto.getAdditionalProcesses())
                .build();

        return repository.save(entity);
    }

    @Override
    public ProcessEntryEntity updateEntry(Long id, ProcessEntryDto dto) {
        ProcessEntryEntity existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Process entry not found with ID: " + id));

        existing.setProcessType(dto.getProcessType());
        existing.setOperationType(dto.getOperationType());
        existing.setPackingType(dto.getPackingType());
        existing.setPackingStyle(dto.getPackingStyle());
        existing.setMode(dto.getMode());
        existing.setAdditionalProcesses(dto.getAdditionalProcesses());

        return repository.save(existing);
    }


    @Override
    public Page<ProcessEntryEntity> getAllEntries(Pageable pageable) {
        return repository.findAll(pageable);
    }


    @Override
    public ProcessEntryEntity getEntryById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public ProcessEntryEntity deleteEntry(Long id) {
        Optional<ProcessEntryEntity> optionalEntity = repository.findById(id);
        if (optionalEntity.isPresent()) {
            ProcessEntryEntity entity = optionalEntity.get();
            repository.delete(entity);
            return entity;
        }
        return null;
    }

    @Override
    public List<ProcessEntryEntity> getAllEntriesWithoutPagination() {
        return repository.findAll();
    }

}
