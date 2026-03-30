package com.indona.invento.dao;

import com.indona.invento.entities.PackingSubmission;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PackingSubmissionRepository extends JpaRepository<PackingSubmission, Long> {

    PackingSubmission findBySoNumberAndLineNumber(String soNumber, String lineNumber);

    List<PackingSubmission> findByPackingId(String packingId);

    @Query(value = "SELECT MAX(packing_id) FROM packing_submission", nativeQuery = true)
    String findLastPackingId();
}
