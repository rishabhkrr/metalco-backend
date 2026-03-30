package com.indona.invento.dao;

import com.indona.invento.entities.ProcessEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessEntryRepository extends JpaRepository<ProcessEntryEntity, Long> {
}

