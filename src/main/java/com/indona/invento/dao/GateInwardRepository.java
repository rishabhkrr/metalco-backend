package com.indona.invento.dao;

import com.indona.invento.entities.GateInwardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GateInwardRepository extends JpaRepository<GateInwardEntity, Long> {
	Optional<GateInwardEntity> findByGatePassRefNumber(String gatePassRefNumber);

	List<GateInwardEntity> findByUnitCode(String unitCode);

	List<GateInwardEntity> findByStatus(String status);

	Optional<GateInwardEntity> findTopByVehicleNumberOrderByTimeStampDesc(String vehicleNumber);

	@Query("SELECT DISTINCT g.vehicleNumber FROM GateInwardEntity g WHERE LOWER(g.status) = LOWER(:status)")
	List<String> findDistinctVehicleNumbersByStatus(@Param("status") String status);

	boolean existsByPoNumbersContaining(String poNumber);

	Optional<GateInwardEntity> findTopByInvoiceNumberOrderByTimeStampDesc(String invoiceNumber);

	@Query("SELECT DISTINCT g.invoiceNumber FROM GateInwardEntity g WHERE LOWER(g.status) = LOWER(:status)")
	List<String> findDistinctInvoiceNumbersByStatus(@Param("status") String status);

	List<GateInwardEntity> findByDcNumberIn(List<String> dcNumbers);

	List<GateInwardEntity> findByInvoiceNumber(String invoiceNumber);

	@Query("SELECT DISTINCT g.invoiceNumber FROM GateInwardEntity g WHERE g.invoiceNumber IS NOT NULL")
	List<String> findDistinctInvoiceNumbers();

    List<GateInwardEntity> findByVehicleOutStatus(String vehicleOutStatus);

    @Query("SELECT g FROM GateInwardEntity g WHERE g.invoiceNumber = :invoiceNumber")
    List<GateInwardEntity> fetchAllByInvoiceNumberCustom(@Param("invoiceNumber") String invoiceNumber);



}
