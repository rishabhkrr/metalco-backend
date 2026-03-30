package com.indona.invento.dao;

import com.indona.invento.entities.GradeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GradeRepository extends JpaRepository<GradeEntity, Long> {
    boolean existsByGradeValue(String gradeValue);
}
