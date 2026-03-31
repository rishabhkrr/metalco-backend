package com.indona.invento.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.indona.invento.entities.AdjustmentEntity;
import com.indona.invento.entities.SkuPriceEntity;
import com.indona.invento.entities.StockinEntity;
  
@Repository
public interface SkuPriceRepository extends JpaRepository<SkuPriceEntity, Long> {

}