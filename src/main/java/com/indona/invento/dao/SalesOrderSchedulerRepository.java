package com.indona.invento.dao;

import com.indona.invento.entities.SalesOrderSchedulerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalesOrderSchedulerRepository extends JpaRepository<SalesOrderSchedulerEntity, Long> {
    List<SalesOrderSchedulerEntity> findBySoNumber(String soNumber);

    @Query("SELECT s FROM SalesOrderSchedulerEntity s " +
            "WHERE UPPER(s.nextProcess) = 'MARKING & CUTTING' " +
            "AND UPPER(s.retrievalStatus) = 'COMPLETE'")
    List<SalesOrderSchedulerEntity> findAllMarkingAndCuttingAndRetrievalStatusCompleted();


    SalesOrderSchedulerEntity findBySoNumberAndLineNumber(String soNumber, String lineNumber);

}

