package com.indona.invento.dao;

import com.indona.invento.entities.SalesmanMasterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SalesmanMasterRepository extends JpaRepository<SalesmanMasterEntity, Long> {
    SalesmanMasterEntity findByUserName(String userName);

    SalesmanMasterEntity findByUserId(String userId);
}
