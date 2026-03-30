package com.indona.invento.dao;

import com.indona.invento.entities.MaterialRequestHeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface MaterialRequestHeaderRepository extends JpaRepository<MaterialRequestHeader, Long> {


    @Query("SELECT COUNT(m) FROM MaterialRequestHeader m WHERE CAST(m.timestamp AS date) = :date")
    int countByDate(@Param("date") LocalDate date);

}
