package com.indona.invento.dao;

import com.indona.invento.entities.POManagementApprovalEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface POManagementApprovalRepository extends JpaRepository<POManagementApprovalEntity, Long> {

    Optional<POManagementApprovalEntity> findByPoNumber(String poNumber);

}

