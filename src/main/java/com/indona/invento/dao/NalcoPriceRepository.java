package com.indona.invento.dao;

import com.indona.invento.entities.NalcoPriceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface NalcoPriceRepository extends JpaRepository<NalcoPriceEntity, Long> {
    Optional<NalcoPriceEntity> findTopByOrderByDateDesc();

    Optional<NalcoPriceEntity> findByDate(LocalDate date);

    List<NalcoPriceEntity> findByDateGreaterThan(LocalDate date);


}