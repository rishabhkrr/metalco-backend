package com.indona.invento.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.indona.invento.entities.BinDamageEntity;

@Repository
public interface BinDamageRepository extends JpaRepository<BinDamageEntity, Long> {
    // You can add custom query methods here if needed
}
