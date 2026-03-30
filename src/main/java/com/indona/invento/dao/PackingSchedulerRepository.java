package com.indona.invento.dao;

import com.indona.invento.entities.PackingEntityScheduler;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PackingSchedulerRepository extends JpaRepository<PackingEntityScheduler, Long> {
    PackingEntityScheduler findBySoNumberAndLineNumber(String soNumber, String lineNumber);
}
