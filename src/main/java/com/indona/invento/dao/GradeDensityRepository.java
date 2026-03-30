package com.indona.invento.dao;

import com.indona.invento.entities.GradeDensityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GradeDensityRepository extends JpaRepository<GradeDensityEntity, Long> {
    Optional<GradeDensityEntity> findByGrade(String grade);
}
