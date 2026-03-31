package com.indona.invento.dao;

import com.indona.invento.entities.RawMaterialQrEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RawMaterialQrRepository extends JpaRepository<RawMaterialQrEntity, Long> {

    Optional<RawMaterialQrEntity> findByRawMaterialQrId(String rawMaterialQrId);
}
