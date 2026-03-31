package com.indona.invento.dao;

import com.indona.invento.entities.PackingListCreationIUMTEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PackingListCreationIUMTRepository extends JpaRepository<PackingListCreationIUMTEntity, Long> {

    @Query("SELECT DISTINCT p.packingListNo FROM PackingListCreationIUMTEntity p WHERE p.packingListNo IS NOT NULL")
    List<String> findPackingListNo();

    List<PackingListCreationIUMTEntity> findAllByPackingListNo(String packingListNo);

    // FRD: PLS-001 — Summary by unit
    List<PackingListCreationIUMTEntity> findByUnit(String unit);

    // FRD: PLS-002 — Find by requesting unit code
    List<PackingListCreationIUMTEntity> findByRequestingUnitUnitCode(String requestingUnitCode);

    // FRD: Summary grouped by IU RFD List Number
    @Query("SELECT DISTINCT p.packingListNo FROM PackingListCreationIUMTEntity p WHERE p.unit = :unit AND p.packingListNo IS NOT NULL")
    List<String> findPackingListNoByUnit(@Param("unit") String unit);
}
