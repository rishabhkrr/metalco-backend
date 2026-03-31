package com.indona.invento.dao;

import com.indona.invento.entities.SoUpdate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SoUpdateRepository extends JpaRepository<SoUpdate, Long> {

    Optional<SoUpdate> findBySoNumber(String soNumber);
}
