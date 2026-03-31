package com.indona.invento.dao;

import com.indona.invento.entities.BankDetailsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BankDetailsRepository extends JpaRepository<BankDetailsEntity, Long> {
    List<BankDetailsEntity> findBySupplierId(Long supplierId);
}