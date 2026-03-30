package com.indona.invento.dao;

import com.indona.invento.entities.GRNItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GRNItemRepository extends JpaRepository<GRNItemEntity, Long> {
}
