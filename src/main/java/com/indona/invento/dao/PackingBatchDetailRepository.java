package com.indona.invento.dao;

import com.indona.invento.entities.PackingBatchDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PackingBatchDetailRepository extends JpaRepository<PackingBatchDetail, Long> {

    List<PackingBatchDetail> findByPackingSubmissionId(Long packingSubmissionId);

    void deleteByPackingSubmissionId(Long packingSubmissionId);
}

