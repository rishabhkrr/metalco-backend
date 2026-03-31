package com.indona.invento.services;

import com.indona.invento.dto.ProcessEntryDto;
import com.indona.invento.entities.ProcessEntryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProcessEntryService {
    ProcessEntryEntity addEntry(ProcessEntryDto dto);
    ProcessEntryEntity updateEntry(Long id, ProcessEntryDto dto);
    Page<ProcessEntryEntity> getAllEntries(Pageable pageable);
    ProcessEntryEntity getEntryById(Long id);
    ProcessEntryEntity deleteEntry(Long id);
    List<ProcessEntryEntity> getAllEntriesWithoutPagination();

}
