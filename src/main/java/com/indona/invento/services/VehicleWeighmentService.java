package com.indona.invento.services;

import com.indona.invento.dao.GateEntryPackingAndDispatchRepository;
import com.indona.invento.dto.VehicleWeighmentRequestDTO;
import com.indona.invento.dto.VehicleWeighmentResponseDTO;

import java.util.List;

public interface VehicleWeighmentService {

    VehicleWeighmentResponseDTO createWeighment(VehicleWeighmentRequestDTO requestDTO);

    VehicleWeighmentResponseDTO getWeighmentById(Long id);

    VehicleWeighmentResponseDTO getWeighmentByRefNumber(String weighmentRefNumber);

    List<VehicleWeighmentResponseDTO> getAllWeighments();

    List<VehicleWeighmentResponseDTO> getWeighmentsByUnit(String unitCode);

    VehicleWeighmentResponseDTO updateWeighment(Long id, VehicleWeighmentRequestDTO requestDTO);

    VehicleWeighmentResponseDTO verifyWeighment(Long id);

    VehicleWeighmentResponseDTO getWeighmentByVehicleNumber(String vehicleNumber);

    List<String> getVehicleNumbersByGateStatus(String status);

    List<String> getAllVehicleNumbersWithInStatus();

    VehicleWeighmentResponseDTO saveInvoiceNumber(String vehicleNumber, String gateEntryRefNo, String invoiceNumber);

}
