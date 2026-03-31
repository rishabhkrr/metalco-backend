package com.indona.invento.dao;

import com.indona.invento.entities.SubContractorMasterEntity;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SubContractorMasterRepository extends JpaRepository<SubContractorMasterEntity, Long> {
    boolean existsBySubContractorCode(String subContractorCode);
    boolean existsBySubContractorNameIgnoreCase(String subContractorName);
    
    @Query("SELECT s.subContractorCode FROM SubContractorMasterEntity s")
    List<String> findAllSubContractorCodes();

    @Query("SELECT s.subContractorCode FROM SubContractorMasterEntity s WHERE s.status = 'APPROVED'")
    List<String> findAllApprovedSubContractorCodes();

    @Query("SELECT s.subContractorName FROM SubContractorMasterEntity s WHERE s.status = 'APPROVED'")
    List<String> findAllApprovedSubContractorNames();


    Optional<SubContractorMasterEntity> findBySubContractorCode(String subContractorCode);

    Optional<SubContractorMasterEntity> findBySubContractorName(String subContractorName);
}
