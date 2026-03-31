package com.indona.invento.dao;

import com.indona.invento.entities.ScrapSummaryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScrapSummaryRepository extends JpaRepository<ScrapSummaryEntity, Long> {
}

