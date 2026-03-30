package com.indona.invento.dao;

import com.indona.invento.entities.VehicleWeighmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleWeighmentRepository extends JpaRepository<VehicleWeighmentEntity, Long> {

    Optional<VehicleWeighmentEntity> findByWeightmentRefNumber(String weightmentRefNumber);

    List<VehicleWeighmentEntity> findByUnit(String unitCode);

    Optional<VehicleWeighmentEntity> findTopByVehicleNumberOrderByTimeStampDesc(String vehicleNumber);

    List<VehicleWeighmentEntity> findByInvoiceNumber(String invoiceNumber);

    @Query("SELECT DISTINCT v.vehicleNumber FROM VehicleWeighmentEntity v")
    List<String> findAllVehicleNumbers();

}
