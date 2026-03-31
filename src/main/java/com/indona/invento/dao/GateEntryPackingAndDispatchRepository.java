package com.indona.invento.dao;

import com.indona.invento.entities.GateEntryPackingAndDispatch;

import java.util.List;
import java.util.Optional;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GateEntryPackingAndDispatchRepository extends JpaRepository<GateEntryPackingAndDispatch, Long> {
	@Query("SELECT g FROM GateEntryPackingAndDispatch g WHERE LOWER(g.vehicleOutStatusPackingAndDispatch) = LOWER(:status)")
    List<GateEntryPackingAndDispatch> findByVehicleOutStatusPackingAndDispatchIgnoreCase(@Param("status") String status);

    @Query("SELECT g FROM GateEntryPackingAndDispatch g WHERE g.gateEntryRefNoPackingAndDispatch = :refNo")
    Optional<GateEntryPackingAndDispatch> findByGateEntryRefNoPackingAndDispatch(@Param("refNo") String refNo);

    @Query("""
    SELECT g
    FROM GateEntryPackingAndDispatch g
    WHERE g.vehicleNumberPackingAndDispatch = :vehicleNumber
""")
    Optional<GateEntryPackingAndDispatch> findLatestByVehicleNumber(@Param("vehicleNumber") String vehicleNumber);
}
