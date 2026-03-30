package com.indona.invento.dao;

import com.indona.invento.entities.UnitMasterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UnitMasterRepository extends JpaRepository<UnitMasterEntity, Long> {
    boolean existsByUnitCode(String code);

    Optional<UnitMasterEntity> findByUnitCode(String unitCode);
    Optional<UnitMasterEntity> findByUnitName(String unitName);

    @Query("SELECT u.unitCode FROM UnitMasterEntity u WHERE LOWER(u.unitName) = LOWER(:unitName)")
    List<String> findUnitCodesByName(@Param("unitName") String unitName);

    @Query("SELECT u.unitCode FROM UnitMasterEntity u WHERE LOWER(u.unitName) = LOWER(:unitName)")
    Optional<String> findUnitCodeByUnitName(@Param("unitName") String unitName);

    Optional<UnitMasterEntity> findByUnitCodeAndUnitName(String unitCode, String unitName);

    @Query("SELECT u.unitCode FROM UnitMasterEntity u WHERE u.status = 'APPROVED'")
    List<String> findAllApprovedUnitCodes();

    //UnitMasterEntity findByUnitName(String unitName);

}
