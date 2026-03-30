package com.indona.invento.dao;

import com.indona.invento.entities.HindalcoPriceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface HindalcoPriceRepository extends JpaRepository<HindalcoPriceEntity, Long> {

    Optional<HindalcoPriceEntity> findByPriceDate(Date priceDate);

    Optional<HindalcoPriceEntity> findTopByOrderByPriceDateDesc();

    List<HindalcoPriceEntity> findByPriceDateAfter(Date priceDate);

    List<HindalcoPriceEntity> findByPriceDateGreaterThanEqual(Date priceDate);
    List<HindalcoPriceEntity> findByPriceDateGreaterThan(Date priceDate);



}
