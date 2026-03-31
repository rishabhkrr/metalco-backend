package com.indona.invento.services.impl;

import com.indona.invento.dao.GateEntryPackingAndDispatchRepository;
import com.indona.invento.dto.VehicleWeighmentRequestDTO;
import com.indona.invento.dto.VehicleWeighmentResponseDTO;
import com.indona.invento.entities.GateEntryPackingAndDispatch;
import com.indona.invento.entities.VehicleWeighmentEntity;
import com.indona.invento.dao.VehicleWeighmentRepository;
import com.indona.invento.services.VehicleWeighmentService;
import com.indona.invento.dao.GateInwardRepository;
import com.indona.invento.entities.GateInwardEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VehicleWeighmentServiceImpl implements VehicleWeighmentService {

    private final VehicleWeighmentRepository vehicleWeighmentRepository;
    private final GateInwardRepository gateInwardRepository;
    private final GateEntryPackingAndDispatchRepository packingRepository;

    @Override
    public VehicleWeighmentResponseDTO createWeighment(VehicleWeighmentRequestDTO requestDTO) {
        VehicleWeighmentEntity entity = new VehicleWeighmentEntity();
        entity.setWeightmentRefNumber(generateRefNumber());
        entity.setUnit(requestDTO.getUnit());
        entity.setVehicleNumber(requestDTO.getVehicleNumber());
        entity.setInvoiceNumber(requestDTO.getInvoiceNumber());
        entity.setPurpose(requestDTO.getPurpose());
        entity.setMode(requestDTO.getMode());

        // Convert List to JSON string for storage
        ObjectMapper mapper = new ObjectMapper();
        try {
            entity.setMedcOrDcNumbers(requestDTO.getMedcOrDcNumbers() != null ?
                mapper.writeValueAsString(requestDTO.getMedcOrDcNumbers()) : null);
            entity.setPoNumbers(requestDTO.getPoNumbers() != null ?
                mapper.writeValueAsString(requestDTO.getPoNumbers()) : null);
            entity.setMedciNumbers(requestDTO.getMedciNumbers() != null ?
                mapper.writeValueAsString(requestDTO.getMedciNumbers()) : null);
        } catch (Exception e) {
            System.out.println("Error converting lists to JSON: " + e.getMessage());
        }

        entity.setDcNumber(requestDTO.getDcNumber());
        entity.setMedcpNumber(requestDTO.getMedcpNumber());
        entity.setGateEntryRefNo(requestDTO.getGateEntryRefNo());
        entity.setVehiclePhotoWithLoad(requestDTO.getVehiclePhotoWithLoad());
        entity.setVehiclePhotoEmpty(requestDTO.getVehiclePhotoEmpty());
        entity.setLoadWeight(requestDTO.getLoadWeight());
        entity.setEmptyWeight(requestDTO.getEmptyWeight());
        entity.setVerified(requestDTO.getVerified() != null ? requestDTO.getVerified() : false);
        entity.setUserId(requestDTO.getUserId());

        VehicleWeighmentEntity saved = vehicleWeighmentRepository.save(entity);

        // Handle purpose-based status update
        updateWeighmentStatusBasedOnPurpose(saved, requestDTO);

        updateGateInwardWeighmentStatus(saved);
        return mapToResponse(saved);
    }

    private void updateWeighmentStatusBasedOnPurpose(VehicleWeighmentEntity weighment, VehicleWeighmentRequestDTO requestDTO) {
        System.out.println("\n🔍 Processing Vehicle Weighment Status Update...");
        System.out.println("   - Purpose: " + weighment.getPurpose());
        System.out.println("   - Vehicle Number: " + weighment.getVehicleNumber());
        System.out.println("   - Gate Entry Ref No: " + weighment.getGateEntryRefNo());

        if ("delivery".equalsIgnoreCase(weighment.getPurpose())) {
            System.out.println("\n📦 DELIVERY Purpose Detected");
            System.out.println("   🔍 Searching in GateInward...");

            // Find in gate-inward using vehicle number + gateEntryRefNo
            String vehicleNumber = weighment.getVehicleNumber();
            String gateEntryRefNo = weighment.getGateEntryRefNo();

            if (vehicleNumber != null && gateEntryRefNo != null) {
                gateInwardRepository.findAll().stream()
                        .filter(g -> vehicleNumber.equalsIgnoreCase(g.getVehicleNumber()) &&
                                   gateEntryRefNo.equalsIgnoreCase(g.getGatePassRefNumber()))
                        .forEach(gate -> {
                            System.out.println("      ✅ Found GateInward Entry");
                            System.out.println("         - Gate ID: " + gate.getId());
                            System.out.println("         - Gate Inward Number: " + gate.getGatePassRefNumber());
                            System.out.println("         - Vehicle: " + gate.getVehicleNumber());

                            // Set vehicle weighment status to PARTIALLY_COMPLETED
                            gate.setVehicleWeighmentStatus("PARTIALLY_COMPLETED");
                            gateInwardRepository.save(gate);

                            System.out.println("      ✅ Updated Status to: PARTIALLY_COMPLETED");
                        });
            }
        }
        else if ("pickup".equalsIgnoreCase(weighment.getPurpose())) {
            System.out.println("\n📤 PICKUP Purpose Detected");
            System.out.println("   🔍 Searching in GateEntryPackingAndDispatch...");

            // Find in gate-entry-packing-and-dispatch using ref no + vehicle number
            String vehicleNumber = weighment.getVehicleNumber();
            String refNo = requestDTO.getGateEntryRefNo();

            if (vehicleNumber != null && refNo != null) {
                packingRepository.findAll().stream()
                        .filter(p -> vehicleNumber.equalsIgnoreCase(p.getVehicleNumberPackingAndDispatch()) &&
                                   refNo.equalsIgnoreCase(p.getGateEntryRefNoPackingAndDispatch()))
                        .forEach(packing -> {
                            System.out.println("      ✅ Found PackingAndDispatch Entry");
                            System.out.println("         - Packing ID: " + packing.getId());
                            System.out.println("         - Reference Number: " + packing.getGateEntryRefNoPackingAndDispatch());
                            System.out.println("         - Vehicle: " + packing.getVehicleNumberPackingAndDispatch());

                            // Set vehicle weighment status to PARTIALLY_COMPLETED
                            packing.setVehicleWeighmentStatusPackingAndDispatch("PARTIALLY_COMPLETED");
                            packingRepository.save(packing);

                            System.out.println("      ✅ Updated Status to: PARTIALLY_COMPLETED");
                        });
            }
        }
        else {
            System.out.println("   ℹ️ No specific purpose matched (not delivery or pickup)");
        }

        System.out.println("\n✅ Purpose-based status update complete\n");
    }


    @Override
    public VehicleWeighmentResponseDTO getWeighmentById(Long id) {
        return vehicleWeighmentRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new RuntimeException("Weighment not found with id: " + id));
    }

    @Override
    public VehicleWeighmentResponseDTO getWeighmentByRefNumber(String weighmentRefNumber) {
        VehicleWeighmentEntity entity = vehicleWeighmentRepository.findByWeightmentRefNumber(weighmentRefNumber)
                .orElseThrow(() -> new RuntimeException("Weighment not found with ref: " + weighmentRefNumber));
        return mapToResponse(entity);
    }

    @Override
    public List<VehicleWeighmentResponseDTO> getAllWeighments() {
        return vehicleWeighmentRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<VehicleWeighmentResponseDTO> getWeighmentsByUnit(String unitCode) {
        return vehicleWeighmentRepository.findByUnit(unitCode).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public VehicleWeighmentResponseDTO updateWeighment(Long id, VehicleWeighmentRequestDTO requestDTO) {
        VehicleWeighmentEntity entity = vehicleWeighmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Weighment not found with id: " + id));

        // Update all fields from request (same as createWeighment)
        entity.setUnit(requestDTO.getUnit());
        entity.setVehicleNumber(requestDTO.getVehicleNumber());
        entity.setInvoiceNumber(requestDTO.getInvoiceNumber());
        entity.setPurpose(requestDTO.getPurpose());
        entity.setMode(requestDTO.getMode());
        entity.setDcNumber(requestDTO.getDcNumber());
        entity.setMedcpNumber(requestDTO.getMedcpNumber());
        entity.setGateEntryRefNo(requestDTO.getGateEntryRefNo());
        entity.setVehiclePhotoWithLoad(requestDTO.getVehiclePhotoWithLoad());
        entity.setVehiclePhotoEmpty(requestDTO.getVehiclePhotoEmpty());
        entity.setLoadWeight(requestDTO.getLoadWeight());
        entity.setEmptyWeight(requestDTO.getEmptyWeight());
        entity.setVerified(requestDTO.getVerified() != null ? requestDTO.getVerified() : false);
        entity.setUserId(requestDTO.getUserId());

        // Convert List to JSON string for storage
        ObjectMapper mapper = new ObjectMapper();
        try {
            entity.setMedcOrDcNumbers(requestDTO.getMedcOrDcNumbers() != null ?
                mapper.writeValueAsString(requestDTO.getMedcOrDcNumbers()) : null);
            entity.setPoNumbers(requestDTO.getPoNumbers() != null ?
                mapper.writeValueAsString(requestDTO.getPoNumbers()) : null);
            entity.setMedciNumbers(requestDTO.getMedciNumbers() != null ?
                mapper.writeValueAsString(requestDTO.getMedciNumbers()) : null);
        } catch (Exception e) {
            System.out.println("Error converting lists to JSON: " + e.getMessage());
        }

        entity.setUpdatedAt(new Date());

        VehicleWeighmentEntity updated = vehicleWeighmentRepository.save(entity);

        // Update weighment status to COMPLETED based on purpose
        updateWeighmentStatusToCompleted(updated, requestDTO);

        updateGateInwardWeighmentStatus(updated);
        return mapToResponse(updated);
    }

    private void updateWeighmentStatusToCompleted(VehicleWeighmentEntity weighment, VehicleWeighmentRequestDTO requestDTO) {
        System.out.println("\n🔍 Processing Vehicle Weighment Status Update to COMPLETED...");
        System.out.println("   - Purpose: " + weighment.getPurpose());
        System.out.println("   - Vehicle Number: " + weighment.getVehicleNumber());
        System.out.println("   - Gate Entry Ref No: " + requestDTO.getGateEntryRefNo());

        String vehicleNumber = weighment.getVehicleNumber();
        String refNo = requestDTO.getGateEntryRefNo();

        if (vehicleNumber == null || refNo == null) {
            System.out.println("   ⚠️ Vehicle number or Ref No is null, skipping status update");
            return;
        }

        if ("delivery".equalsIgnoreCase(weighment.getPurpose())) {
            System.out.println("\n📦 DELIVERY Purpose Detected - Updating GateInward");

            gateInwardRepository.findAll().stream()
                    .filter(g -> vehicleNumber.equalsIgnoreCase(g.getVehicleNumber()) &&
                               refNo.equalsIgnoreCase(g.getGatePassRefNumber()))
                    .forEach(gate -> {
                        System.out.println("      ✅ Found GateInward Entry");
                        System.out.println("         - Gate ID: " + gate.getId());
                        System.out.println("         - Gate Pass Ref Number: " + gate.getGatePassRefNumber());
                        System.out.println("         - Vehicle: " + gate.getVehicleNumber());

                        // Set vehicle weighment status to COMPLETED
                        gate.setVehicleWeighmentStatus("COMPLETED");
                        gateInwardRepository.save(gate);

                        System.out.println("      ✅ Updated Status to: COMPLETED");
                    });
        }
        else if ("pickup".equalsIgnoreCase(weighment.getPurpose())) {
            System.out.println("\n📤 PICKUP Purpose Detected - Updating GateEntryPackingAndDispatch");

            packingRepository.findAll().stream()
                    .filter(p -> vehicleNumber.equalsIgnoreCase(p.getVehicleNumberPackingAndDispatch()) &&
                               refNo.equalsIgnoreCase(p.getGateEntryRefNoPackingAndDispatch()))
                    .forEach(packing -> {
                        System.out.println("      ✅ Found PackingAndDispatch Entry");
                        System.out.println("         - Packing ID: " + packing.getId());
                        System.out.println("         - Reference Number: " + packing.getGateEntryRefNoPackingAndDispatch());
                        System.out.println("         - Vehicle: " + packing.getVehicleNumberPackingAndDispatch());

                        // Set vehicle weighment status to COMPLETED
                        packing.setVehicleWeighmentStatusPackingAndDispatch("COMPLETED");
                        packingRepository.save(packing);

                        System.out.println("      ✅ Updated Status to: COMPLETED");
                    });
        }
        else {
            System.out.println("   ℹ️ No specific purpose matched (not delivery or pickup)");
        }

        System.out.println("\n✅ Weighment status update to COMPLETED complete\n");
    }

    @Override
    public VehicleWeighmentResponseDTO verifyWeighment(Long id) {
        VehicleWeighmentEntity entity = vehicleWeighmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Weighment not found with id: " + id));
        entity.setVerified(true);
        VehicleWeighmentEntity updated = vehicleWeighmentRepository.save(entity);
        return mapToResponse(updated);
    }

    @Override
    public VehicleWeighmentResponseDTO getWeighmentByVehicleNumber(String vehicleNumber) {
        Optional<GateInwardEntity> gateInward = gateInwardRepository.findTopByVehicleNumberOrderByTimeStampDesc(vehicleNumber);
        if (gateInward.isEmpty()) {
            throw new RuntimeException("No Gate Inward found for vehicle: " + vehicleNumber);
        }

        GateInwardEntity inward = gateInward.get();
        VehicleWeighmentResponseDTO dto = new VehicleWeighmentResponseDTO();
        dto.setVehicleNumber(vehicleNumber);
        dto.setInvoiceNumber(inward.getInvoiceNumber());
        dto.setGateEntryRefNo(inward.getGatePassRefNumber());
        dto.setUnit(inward.getUnitCode());
        dto.setMode(inward.getMode());

        if (inward.getPoNumbers() != null && !inward.getPoNumbers().isEmpty()) {
            dto.setPoNumbers(inward.getPoNumbers());
        }

        return dto;
    }


    // ---------- Utility Methods ----------

    private String generateRefNumber() {
        String prefix = "MEWB";
        String datePart = new SimpleDateFormat("ddMM").format(new Date());
        long count = vehicleWeighmentRepository.count() + 1;
        return prefix + datePart + String.format("%04d", count);
    }

    private VehicleWeighmentResponseDTO mapToResponse(VehicleWeighmentEntity entity) {
        VehicleWeighmentResponseDTO dto = new VehicleWeighmentResponseDTO();
        dto.setId(entity.getId());
        dto.setWeightmentRefNumber(entity.getWeightmentRefNumber());
        dto.setUnit(entity.getUnit());
        dto.setVehicleNumber(entity.getVehicleNumber());
        dto.setInvoiceNumber(entity.getInvoiceNumber());
        dto.setGateEntryRefNo(entity.getGateEntryRefNo());
        dto.setVehiclePhotoWithLoad(entity.getVehiclePhotoWithLoad());
        dto.setVehiclePhotoEmpty(entity.getVehiclePhotoEmpty());
        dto.setLoadWeight(entity.getLoadWeight());
        dto.setEmptyWeight(entity.getEmptyWeight());
        dto.setVerified(entity.getVerified());
        dto.setUserId(entity.getUserId());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setPurpose(entity.getPurpose());
        dto.setMode(entity.getMode());
        dto.setDcNumber(entity.getDcNumber());
        dto.setMedcpNumber(entity.getMedcpNumber());

        // Convert JSON strings back to Lists
        ObjectMapper mapper = new ObjectMapper();
        try {
            if (entity.getMedcOrDcNumbers() != null) {
                List<String> medcList = mapper.readValue(entity.getMedcOrDcNumbers(),
                    mapper.getTypeFactory().constructCollectionType(List.class, String.class));
                dto.setMedcOrDcNumbers(medcList);
            }
            if (entity.getPoNumbers() != null) {
                List<String> poList = mapper.readValue(entity.getPoNumbers(),
                    mapper.getTypeFactory().constructCollectionType(List.class, String.class));
                dto.setPoNumbers(poList);
            }
            if (entity.getMedciNumbers() != null) {
                List<String> medciList = mapper.readValue(entity.getMedciNumbers(),
                    mapper.getTypeFactory().constructCollectionType(List.class, String.class));
                dto.setMedciNumbers(medciList);
            }
        } catch (Exception e) {
            System.out.println("Error converting JSON to lists: " + e.getMessage());
        }

        gateInwardRepository.findTopByVehicleNumberOrderByTimeStampDesc(entity.getVehicleNumber())
                .ifPresent(inward -> {
                    // Only set mode from gate inward if not already set from entity
                    if (dto.getMode() == null) {
                        dto.setMode(inward.getMode());
                    }
                });
        return dto;
    }

    @Override
    public List<String> getVehicleNumbersByGateStatus(String status) {
        return gateInwardRepository.findDistinctVehicleNumbersByStatus(status);
    }

    private void updateGateInwardWeighmentStatus(VehicleWeighmentEntity vw) {

        gateInwardRepository.findTopByInvoiceNumberOrderByTimeStampDesc(vw.getInvoiceNumber())
                .ifPresent(gate -> {

                    boolean hasLoad = vw.getLoadWeight() != null;
                    boolean hasEmpty = vw.getEmptyWeight() != null;

                    if (hasLoad && hasEmpty) {
                        gate.setVehicleWeighmentStatus("COMPLETED");
                    } else if (hasLoad || hasEmpty) {
                        gate.setVehicleWeighmentStatus("PARTIALLY COMPLETED");
                    } else {
                        gate.setVehicleWeighmentStatus("PENDING");
                    }

                    gateInwardRepository.save(gate);
                });
    }

    @Override
    public List<String> getAllVehicleNumbersWithInStatus() {

        // 1️⃣ Vehicles already present in weighment (exclude these)
        Set<String> weighedVehicles = vehicleWeighmentRepository
                .findAllVehicleNumbers()
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 2️⃣ Gate Inward vehicles with vehicleOutStatus = IN
        Set<String> gateInwardVehicles = gateInwardRepository
                .findByVehicleOutStatus("IN")
                .stream()
                .map(GateInwardEntity::getVehicleNumber)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 3️⃣ Packing & Dispatch vehicles with vehicleOutStatusPackingAndDispatch = IN
        Set<String> packingVehicles = packingRepository
                .findByVehicleOutStatusPackingAndDispatchIgnoreCase("IN")
                .stream()
                .map(GateEntryPackingAndDispatch::getVehicleNumberPackingAndDispatch)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 4️⃣ Merge both sources
        gateInwardVehicles.addAll(packingVehicles);

        // 5️⃣ Remove vehicles already weighed
        gateInwardVehicles.removeAll(weighedVehicles);

        // 6️⃣ Return final list
        return new ArrayList<>(gateInwardVehicles);
    }

    @Override
    public VehicleWeighmentResponseDTO saveInvoiceNumber(String vehicleNumber, String gateEntryRefNo, String invoiceNumber) {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║   📝 SAVING INVOICE NUMBER             ║");
        System.out.println("╚════════════════════════════════════════╝\n");
        System.out.println("🚗 Vehicle Number: " + vehicleNumber);
        System.out.println("📍 Gate Entry Ref No: " + gateEntryRefNo);
        System.out.println("📄 Invoice Number: " + invoiceNumber);

        try {
            // Find vehicle weighment by vehicle number
            VehicleWeighmentEntity weighment = vehicleWeighmentRepository.findTopByVehicleNumberOrderByTimeStampDesc(vehicleNumber)
                    .orElseThrow(() -> new RuntimeException("Vehicle weighment not found for vehicle: " + vehicleNumber));

            System.out.println("✅ Found vehicle weighment with ID: " + weighment.getId());

            // Update invoice number
            weighment.setInvoiceNumber(invoiceNumber);
            weighment.setGateEntryRefNo(gateEntryRefNo);
            weighment.setUpdatedAt(new java.util.Date());

            // Save to database
            VehicleWeighmentEntity saved = vehicleWeighmentRepository.save(weighment);

            System.out.println("✅ Invoice number saved successfully!");
            System.out.println("📊 Updated weighment ID: " + saved.getId());
            System.out.println("\n╔════════════════════════════════════════╗");
            System.out.println("║     ✅ SAVE COMPLETE                   ║");
            System.out.println("╚════════════════════════════════════════╝\n");

            // Convert to DTO and return
            return mapToResponse(saved);

        } catch (Exception e) {
            System.err.println("❌ Error saving invoice number: " + e.getMessage());
            throw new RuntimeException("Failed to save invoice number: " + e.getMessage());
        }
    }

}
