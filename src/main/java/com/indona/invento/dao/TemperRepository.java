package com.indona.invento.dao;

import com.indona.invento.entities.TemperEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TemperRepository extends JpaRepository<TemperEntity, Long> {
    boolean existsByTemperValue(String temperValue);
}
