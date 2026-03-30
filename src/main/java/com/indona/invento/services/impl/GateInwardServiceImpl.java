package com.indona.invento.services.impl;

import com.indona.invento.dao.*;
import com.indona.invento.dto.*;
import com.indona.invento.entities.*;
import com.indona.invento.services.AuditLogService;
import com.indona.invento.services.GateInwardService;
import com.indona.invento.services.NotificationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GateInwardServiceImpl implements GateInwardService {

    private final GateInwardRepository gateInwardRepository;
    private final GateEntryPackingAndDispatchRepository gateEntryPackingAndDispatchRepository;
    private final POGenerationRepository poGenerationRepository;
    private final DeliveryChallanJWRepository deliveryChallanJWRepository;
    private final VehicleWeighmentRepository vehicleWeighmentRepository;
    private final GRNRepository grnRepository;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private NotificationService notificationService;

    @Override
    public GateInwardResponseDTO createGateInward(GateInwardRequestDTO requestDTO) {
        String gatePassRefNumber = generateGatePassRefNumber();

        GateInwardEntity entity = GateInwardEntity.builder()
                .gatePassRefNumber(gatePassRefNumber)
                .unitCode(requestDTO.getUnitCode())
                .medcNumber(requestDTO.getMedcNumber())
                .mode(requestDTO.getMode())
                .invoiceNumber(requestDTO.getInvoiceNumber())
                .vehicleNumber(requestDTO.getVehicleNumber())
                .driverName(requestDTO.getDriverName())
                .poNumbers(requestDTO.getPoNumbers())
                .dcNumber(requestDTO.getDcNumber())
                .purpose(requestDTO.getPurpose())
                .medciNumber(requestDTO.getMedciNumber())
                .eWayBillNumber(requestDTO.getEWayBillNumber())
                .testCertificates(convertToTestCertificates(requestDTO.getTestCertificates()))
                .vehicleWeighmentStatus(requestDTO.getVehicleWeighmentStatus())
                .materialUnloadingStatus(requestDTO.getMaterialUnloadingStatus())
                .invoiceScanUrls(requestDTO.getInvoiceScanUrls())
                .dcDocumentScanUrls(requestDTO.getDcDocumentScanUrls())
                .testCertificateScanUrls(requestDTO.getTestCertificateScanUrls())
                .eWayBillScanUrls(requestDTO.getEWayBillScanUrls())
                .medciScanUrls(requestDTO.getMedciScanUrls())
                .medcScanUrls(requestDTO.getMedcScanUrls())
                .vehicleDocumentsScanUrls(requestDTO.getVehicleDocumentsScanUrls())
                .vehicleWeighmentScanUrls(requestDTO.getVehicleWeighmentScanUrls())
                .vehicleOutStatus("IN")
                .materialUnloadingStatusUrls(requestDTO.getMaterialUnloadingStatusUrls())
                .status("IN PROGRESS")
                .timeStamp(new Date())
                .build();

        GateInwardEntity saved = gateInwardRepository.save(entity);
        return mapToResponseDTO(saved);
    }

    @Override
    public GateInwardResponseDTO getGateInwardById(Long id) {
        GateInwardEntity entity = gateInwardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Gate Inward not found with ID: " + id));
        return mapToResponseDTO(entity);
    }

    @Override
    public GateInwardResponseDTO getGateInwardByRefNumber(String gatePassRefNumber) {
        GateInwardEntity entity = gateInwardRepository.findByGatePassRefNumber(gatePassRefNumber)
                .orElseThrow(() -> new RuntimeException("Gate Inward not found with ref number: " + gatePassRefNumber));
        return mapToResponseDTO(entity);
    }

    @Override
    public List<GateInwardResponseDTO> getAllGateInwards() {
        return gateInwardRepository.findAll()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<GateInwardResponseDTO> getGateInwardsByUnit(String unitCode) {
        return gateInwardRepository.findByUnitCode(unitCode)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<GateInwardResponseDTO> getGateInwardsByStatus(String status) {
        return gateInwardRepository.findByStatus(status)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public GateInwardResponseDTO updateGateInward(Long id, GateInwardRequestDTO requestDTO) {
        GateInwardEntity entity = gateInwardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Gate Inward not found with ID: " + id));

        // Update status fields
        if (requestDTO.getVehicleWeighmentStatus() != null) {
            entity.setVehicleWeighmentStatus(requestDTO.getVehicleWeighmentStatus());
        }
        if (requestDTO.getMaterialUnloadingStatus() != null) {
            entity.setMaterialUnloadingStatus(requestDTO.getMaterialUnloadingStatus());
        }
        if (requestDTO.getVehicleOutStatus() != null) {
            entity.setVehicleOutStatus(requestDTO.getVehicleOutStatus());
            // If vehicle is marked as OUT, update status to COMPLETED
            if ("OUT".equalsIgnoreCase(requestDTO.getVehicleOutStatus())) {
                entity.setStatus("COMPLETED");
                entity.setGateOutTime(new Date());
            }
        }

        // Update test certificates
        if (requestDTO.getTestCertificates() != null && !requestDTO.getTestCertificates().isEmpty()) {
            List<TCDetails> tcDetailsList = requestDTO.getTestCertificates().stream()
                    .map(dto -> TCDetails.builder()
                            .tcNumber(dto.getTcNumber())
                            .pdfLink(dto.getPdfLink())
                            .build())
                    .collect(Collectors.toList());
            entity.setTestCertificates(tcDetailsList);
        }

        // Update document URLs
        if (requestDTO.getInvoiceScanUrls() != null && !requestDTO.getInvoiceScanUrls().isEmpty()) {
            entity.setInvoiceScanUrls(requestDTO.getInvoiceScanUrls());
        }
        if (requestDTO.getDcDocumentScanUrls() != null && !requestDTO.getDcDocumentScanUrls().isEmpty()) {
            entity.setDcDocumentScanUrls(requestDTO.getDcDocumentScanUrls());
        }
        if(requestDTO.getMedciScanUrls() != null && !requestDTO.getMedciScanUrls().isEmpty()) {
            entity.setMedciScanUrls(requestDTO.getMedciScanUrls());
        }
        if(requestDTO.getMedcScanUrls() != null && !requestDTO.getMedcScanUrls().isEmpty()) {
            entity.setMedcScanUrls(requestDTO.getMedcScanUrls());
        }
        if (requestDTO.getTestCertificateScanUrls() != null && !requestDTO.getTestCertificateScanUrls().isEmpty()) {
            entity.setTestCertificateScanUrls(requestDTO.getTestCertificateScanUrls());
        }
        if (requestDTO.getEWayBillScanUrls() != null && !requestDTO.getEWayBillScanUrls().isEmpty()) {
            entity.setEWayBillScanUrls(requestDTO.getEWayBillScanUrls());
        }
        if (requestDTO.getVehicleDocumentsScanUrls() != null && !requestDTO.getVehicleDocumentsScanUrls().isEmpty()) {
            entity.setVehicleDocumentsScanUrls(requestDTO.getVehicleDocumentsScanUrls());
        }
        if (requestDTO.getVehicleWeighmentScanUrls() != null && !requestDTO.getVehicleWeighmentScanUrls().isEmpty()) {
            entity.setVehicleWeighmentScanUrls(requestDTO.getVehicleWeighmentScanUrls());
        }
        if (requestDTO.getMaterialUnloadingStatusUrls() != null && !requestDTO.getMaterialUnloadingStatusUrls().isEmpty()) {
            entity.setMaterialUnloadingStatusUrls(requestDTO.getMaterialUnloadingStatusUrls());
        }

        GateInwardEntity updated = gateInwardRepository.save(entity);
        return mapToResponseDTO(updated);
    }

    @Override
    public List<POPlacedDTO> getPOsWithPlacedStatus() {
        List<POGenerationEntity> allPlacedPos = poGenerationRepository.findByPoStatus("PO PLACED");

        return allPlacedPos.stream()
                .filter(po -> !checkIfPoAlreadyHasGateEntry(po.getPoNumber()))
                .map(this::mapToPOPlacedDTO)
                .collect(Collectors.toList());
    }

    @Override
    public boolean checkIfPoAlreadyHasGateEntry(String poNumber) {
        return gateInwardRepository.existsByPoNumbersContaining(poNumber);
    }

    private String generateGatePassRefNumber() {
        String prefix = "MEGE";
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        int sequence = 1;
        String refNumber;

        do {
            String sequencePart = String.format("%03d", sequence);
            refNumber = prefix + datePart + sequencePart;
            sequence++;
        } while (gateInwardRepository.findByGatePassRefNumber(refNumber).isPresent());

        return refNumber;
    }

    private GateInwardResponseDTO mapToResponseDTO(GateInwardEntity entity) {
        return GateInwardResponseDTO.builder()
                .id(entity.getId())
                .gatePassRefNumber(entity.getGatePassRefNumber())
                .unitCode(entity.getUnitCode())
                .medcNumber(entity.getMedcNumber())
                .medciNumber(entity.getMedciNumber())
                .mode(entity.getMode())
                .invoiceNumber(entity.getInvoiceNumber())
                .purpose(entity.getPurpose())
                .vehicleNumber(entity.getVehicleNumber())
                .driverName(entity.getDriverName())
                .status(entity.getStatus())
                .timeStamp(entity.getTimeStamp())
                .gateOutTime(entity.getGateOutTime())
                .poNumbers(entity.getPoNumbers())
                .dcNumber(entity.getDcNumber())
                .eWayBillNumber(entity.getEWayBillNumber())
                .testCertificates(convertToTestCertificatesDTO(entity.getTestCertificates()))
                .vehicleWeighmentStatus(entity.getVehicleWeighmentStatus())
                .materialUnloadingStatus(entity.getMaterialUnloadingStatus())
                .vehicleOutStatus(entity.getVehicleOutStatus())
                .invoiceScanUrls(entity.getInvoiceScanUrls())
                .dcDocumentScanUrls(entity.getDcDocumentScanUrls())
                .testCertificateScanUrls(entity.getTestCertificateScanUrls())
                .eWayBillScanUrls(entity.getEWayBillScanUrls())
                .vehicleDocumentsScanUrls(entity.getVehicleDocumentsScanUrls())
                .vehicleWeighmentScanUrls(entity.getVehicleWeighmentScanUrls())
                .medciScanUrls(entity.getMedciScanUrls())
                .medcScanUrls(entity.getMedcScanUrls())
                .materialUnloadingStatusUrls(entity.getMaterialUnloadingStatusUrls())
                .build();
    }

    private POPlacedDTO mapToPOPlacedDTO(POGenerationEntity entity) {
        return POPlacedDTO.builder()
                .id(entity.getId())
                .poNumber(entity.getPoNumber())
                .supplierCode(entity.getSupplierCode())
                .supplierName(entity.getSupplierName())
                .timeStamp(entity.getTimeStamp())
                .billingAddress(entity.getBillingAddress())
                .shippingAddress(entity.getShippingAddress())
                .build();
    }

    private List<TCDetailsDTO> convertToTestCertificatesDTO(List<TCDetails> testCertificates) {
        if (testCertificates == null) {
            return new ArrayList<>();
        }
        return testCertificates.stream()
                .map(tc -> TCDetailsDTO.builder()
                        .tcNumber(tc.getTcNumber())
                        .pdfLink(tc.getPdfLink())
                        .build())
                .collect(Collectors.toList());
    }

    private List<TCDetails> convertToTestCertificates(List<TCDetailsDTO> testCertificateDTOs) {
        if (testCertificateDTOs == null) {
            return new ArrayList<>();
        }
        return testCertificateDTOs.stream()
                .map(dto -> TCDetails.builder()
                        .tcNumber(dto.getTcNumber())
                        .pdfLink(dto.getPdfLink())
                        .build())
                .collect(Collectors.toList());
    }

    
    @Override
    public List<GateInwardInvoiceFetchDTO> getDataByInvoiceNumber(String invoiceNumber) {

        List<GateInwardEntity> gateInwardList =
                gateInwardRepository.findByInvoiceNumber(invoiceNumber);

        List<VehicleWeighmentEntity> vehicleWeighments =
                vehicleWeighmentRepository.findByInvoiceNumber(invoiceNumber);

        List<GateInwardInvoiceFetchDTO> responseList = new ArrayList<>();

        for (GateInwardEntity gate : gateInwardList) {

            DeliveryChallanJWEntity dc =
                    deliveryChallanJWRepository.findByMedcNumber(gate.getMedcNumber());

            VehicleWeighmentEntity vw = vehicleWeighments.stream()
                    .findFirst()   // if multiple, you can adjust logic
                    .orElse(null);

            GateInwardInvoiceFetchDTO dto = GateInwardInvoiceFetchDTO.builder()
                    .gatePassRefNumber(gate.getGatePassRefNumber())
                    .medcNumber(gate.getMedcNumber())
                    .dcNumber(gate.getDcNumber())
                    .eWayBillNumber(gate.getEWayBillNumber())
                    .vehicleNumber(gate.getVehicleNumber())
                    .invoiceScanUrls(gate.getInvoiceScanUrls())
                    .dcDocumentScanUrls(gate.getDcDocumentScanUrls())
                    .eWayBillScanUrls(gate.getEWayBillScanUrls())
                    .vehicleDocumentsScanUrls(gate.getVehicleDocumentsScanUrls())

                    // DeliveryChallanJW details
                    .unit(dc != null ? dc.getUnit() : null)
                    .subContractorCode(dc != null ? dc.getSubContractorCode() : null)
                    .subContractorName(dc != null ? dc.getSubContractorName() : null)

                    // Vehicle Weighment
                    .weightmentRefNumber(vw != null ? vw.getWeightmentRefNumber() : null)
                    .loadWeight(vw != null ? vw.getLoadWeight() : null)
                    .emptyWeight(vw != null ? vw.getEmptyWeight() : null)
                    .build();

            responseList.add(dto);
        }

        return responseList;
    }

    @Override
    public List<String> getDistinctInvoiceNumbers() {
        return gateInwardRepository.findDistinctInvoiceNumbers();
    }


    @Override
    public List<InvoiceDropdownDTO> getInvoiceNumbersWithVehicleInStatus() {

        List<GateInwardEntity> entries = gateInwardRepository.findByVehicleOutStatus("IN");
        return entries.stream()
                .filter(e -> e.getInvoiceNumber() != null)
                .map(e -> new InvoiceDropdownDTO(
                        e.getInvoiceNumber(),
                        e.getMode()
                ))
                .distinct()
                .collect(Collectors.toList());
    }


    @Override
    @Transactional
    public void markMaterialUnloadingCompleteByInvoice(String invoiceNumber) {
        List<GateInwardEntity> entries = gateInwardRepository.fetchAllByInvoiceNumberCustom(invoiceNumber);

        if (entries.isEmpty()) {
            throw new RuntimeException("No entries found for invoice: " + invoiceNumber);
        }

        for (GateInwardEntity entry : entries) {
            entry.setMaterialUnloadingStatus("COMPLETED");
            gateInwardRepository.save(entry);
        }

        //  Update GRN Summary entries linked to this invoice
        List<GRNEntity> grnList = grnRepository.findAllByInvoiceNumber(invoiceNumber);

        for (GRNEntity grn : grnList) {
            grn.setMaterialUnloadingStatus("COMPLETED");
            grnRepository.save(grn);
        }
    }

    @Override
    @Transactional
    public GateInwardEntity updateGateInwardPartialById(Long id, GateInwardPartialUpdateDTO dto) {
        GateInwardEntity entity = gateInwardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Gate Inward not found with ID: " + id));

        if (dto.getUnitCode() != null) entity.setUnitCode(dto.getUnitCode());
        if (dto.getMedcNumber() != null) entity.setMedcNumber(dto.getMedcNumber());
        if(dto.getMedciNumber() != null) entity.setMedciNumber(dto.getMedciNumber());
        if (dto.getMode() != null) entity.setMode(dto.getMode());
        if (dto.getInvoiceNumber() != null) entity.setInvoiceNumber(dto.getInvoiceNumber());
        if (dto.getVehicleNumber() != null) entity.setVehicleNumber(dto.getVehicleNumber());
        if (dto.getDriverName() != null) entity.setDriverName(dto.getDriverName());
        if (dto.getStatus() != null) entity.setStatus(dto.getStatus());
        if (dto.getGateOutTime() != null) entity.setGateOutTime(dto.getGateOutTime());
        if (dto.getDcNumber() != null) entity.setDcNumber(dto.getDcNumber());
        if (dto.getEWayBillNumber() != null) entity.setEWayBillNumber(dto.getEWayBillNumber());
        if (dto.getVehicleWeighmentStatus() != null) entity.setVehicleWeighmentStatus(dto.getVehicleWeighmentStatus());
        if (dto.getMaterialUnloadingStatus() != null) entity.setMaterialUnloadingStatus(dto.getMaterialUnloadingStatus());
        if (dto.getVehicleOutStatus() != null) entity.setVehicleOutStatus(dto.getVehicleOutStatus());

        return gateInwardRepository.save(entity);
    }

    @Override
    public void deleteAllGateInwards() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║     🗑️  DELETE ALL GATE INWARDS       ║");
        System.out.println("╚════════════════════════════════════════╝\n");

        try {
            long totalCount = gateInwardRepository.count();
            System.out.println("📊 Total gate inwards before deletion: " + totalCount);

            gateInwardRepository.deleteAll();

            long afterCount = gateInwardRepository.count();
            System.out.println("✅ All gate inwards deleted successfully!");
            System.out.println("📊 Total gate inwards after deletion: " + afterCount);
            System.out.println("\n╔════════════════════════════════════════╗");
            System.out.println("║     ✅ DELETION COMPLETE               ║");
            System.out.println("╚════════════════════════════════════════╝\n");
        } catch (Exception e) {
            System.err.println("❌ Error deleting all gate inwards: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to delete all gate inwards: " + e.getMessage());
        }
    }

    /**
     * FRD Gate OUT Validation:
     * Vehicle can ONLY exit when BOTH:
     * 1. Vehicle weighment (empty weight) is COMPLETED
     * 2. Material unloading is COMPLETED
     */
    @Override
    public GateInwardResponseDTO markVehicleOut(Long id) {
        GateInwardEntity entity = gateInwardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Gate Inward not found with ID: " + id));

        // Validation 1: Vehicle weighment must be completed
        String weighmentStatus = entity.getVehicleWeighmentStatus();
        if (weighmentStatus == null || !"COMPLETED".equalsIgnoreCase(weighmentStatus)) {
            throw new RuntimeException(
                    "Gate OUT blocked: Vehicle weighment is not completed. " +
                    "Current status: " + (weighmentStatus != null ? weighmentStatus : "NOT_STARTED") +
                    ". Both load and empty weights must be recorded before gate exit.");
        }

        // Validation 2: Material unloading must be completed
        String unloadingStatus = entity.getMaterialUnloadingStatus();
        if (unloadingStatus == null || !"COMPLETED".equalsIgnoreCase(unloadingStatus)) {
            throw new RuntimeException(
                    "Gate OUT blocked: Material unloading is not completed. " +
                    "Current status: " + (unloadingStatus != null ? unloadingStatus : "NOT_STARTED") +
                    ". All materials must be unloaded before gate exit.");
        }

        entity.setVehicleOutStatus("OUT");
        entity.setGateOutTime(new Date());
        entity.setStatus("COMPLETED");

        GateInwardEntity updated = gateInwardRepository.save(entity);

        // Audit log
        auditLogService.logAction("STATUS_CHANGE", "GATE_ENTRY", "GateInward",
                entity.getId(), entity.getGatePassRefNumber(), "IN", "OUT",
                "Vehicle " + entity.getVehicleNumber() + " marked OUT at gate. " +
                "Ref: " + entity.getGatePassRefNumber(),
                "SECURITY", entity.getUnitCode());

        return mapToResponseDTO(updated);
    }

    @Override
    public List<GateInwardSummaryDTO> getAllGateInwardSummary() {
        System.out.println("\n📋 [Gate Entry Summary] Fetching all gate entries from both GATE_INWARD and GATE_ENTRY_PACKING_DISPATCH");

        List<GateInwardSummaryDTO> summaryList = new ArrayList<>();

        // Step 1: Fetch from gate_inward table
        System.out.println("   🔍 Step 1: Fetching from GATE_INWARD table...");
        List<GateInwardEntity> gateInwardEntities = gateInwardRepository.findAll();
        System.out.println("      📊 Gate Inward entries found: " + gateInwardEntities.size());

        for (GateInwardEntity entity : gateInwardEntities) {
            GateInwardSummaryDTO summary = GateInwardSummaryDTO.builder()
                    .id(entity.getId())
                    .source("GATE_INWARD")
                    .vehicleNumber(entity.getVehicleNumber())
                    .driverName(entity.getDriverName())
                    .gateEntryRefNo(entity.getGatePassRefNumber())
                    .purpose(entity.getPurpose())
                    .mode(entity.getMode())
                    .status(entity.getStatus())
                    .gateInTime(entity.getTimeStamp())
                    .gateOutTime(entity.getGateOutTime())
                    .invoiceNumber(entity.getInvoiceNumber())
                    .dcNumber(entity.getDcNumber())
                    .eWayBillNumber(entity.getEWayBillNumber())
                    .poNumbers(entity.getPoNumbers())
                    .medcNumbers(parseMedcNumbers(entity.getMedcNumber()))
                    .medciNumbers(parseMedciNumbers(entity.getMedciNumber()))
                    .medcpNumber(null)
                    .unitCode(entity.getUnitCode())
                    .vehicleWeighmentStatus(entity.getVehicleWeighmentStatus())
                    .materialUnloadingStatus(entity.getMaterialUnloadingStatus())
                    .vehicleOutStatus(entity.getVehicleOutStatus())
                    .build();

            summaryList.add(summary);
            System.out.println("      ✅ Added: Source=GATE_INWARD | Vehicle=" + entity.getVehicleNumber() +
                             " | Invoice=" + entity.getInvoiceNumber());
        }

        System.out.println("   ✅ Gate Inward entries processed: " + gateInwardEntities.size());

        // Step 2: Fetch from gate_entry_packing_and_dispatch table
        System.out.println("   🔍 Step 2: Fetching from GATE_ENTRY_PACKING_AND_DISPATCH table...");

        try {
            // Using reflection to get GateEntryPackingAndDispatchRepository if available
            // This allows the code to work even if the repository changes
            List<com.indona.invento.entities.GateEntryPackingAndDispatch> packingDispatchEntities =
                gateInwardRepository.findAll().isEmpty() ? new ArrayList<>() : fetchGateEntryPackingData();

            System.out.println("      📊 Gate Entry Packing & Dispatch entries found: " + packingDispatchEntities.size());

            for (com.indona.invento.entities.GateEntryPackingAndDispatch entity : packingDispatchEntities) {
                GateInwardSummaryDTO summary = GateInwardSummaryDTO.builder()
                        .id(entity.getId())
                        .source("GATE_ENTRY_PACKING_DISPATCH")
                        .vehicleNumber(entity.getVehicleNumberPackingAndDispatch())
                        .driverName(entity.getDriverNamePackingAndDispatch())
                        .gateEntryRefNo(entity.getGateEntryRefNoPackingAndDispatch())
                        .purpose(entity.getPurposePackingAndDispatch())
                        .mode(entity.getModePackingAndDispatch())
                        .status(entity.getVehicleOutStatusPackingAndDispatch())
                        .gateInTime(entity.getTimeStampPackingAndDispatch() != null ?
                            java.sql.Timestamp.valueOf(entity.getTimeStampPackingAndDispatch()) : null)
                        .gateOutTime(null)
                        .invoiceNumber(entity.getInvoiceNumberPackingAndDispatch())
                        .dcNumber(null)
                        .eWayBillNumber(entity.getEwayBillNumberPackingAndDispatch())
                        .poNumbers(null)
                        .medcNumbers(parseMedcNumbers(entity.getMedcNumberPackingAndDispatch()))
                        .medciNumbers(parseMedciNumbers(entity.getMedciNumberPackingAndDispatch()))
                        .medcpNumber(entity.getMedcpNumberPackingAndDispatch())
                        .unitCode(entity.getUnitPackingAndDispatch())
                        .vehicleWeighmentStatus(entity.getVehicleWeighmentStatusPackingAndDispatch())
                        .materialUnloadingStatus(null)
                        .vehicleOutStatus(entity.getVehicleOutStatusPackingAndDispatch())
                        .build();

                summaryList.add(summary);
                System.out.println("      ✅ Added: Source=GATE_ENTRY_PACKING_DISPATCH | Vehicle=" +
                                 entity.getVehicleNumberPackingAndDispatch() +
                                 " | Invoice=" + entity.getInvoiceNumberPackingAndDispatch());
            }

            System.out.println("   ✅ Gate Entry Packing & Dispatch entries processed: " + packingDispatchEntities.size());
        } catch (Exception e) {
            System.out.println("   ⚠️ Could not fetch GateEntryPackingAndDispatch data: " + e.getMessage());
        }

        System.out.println("   ✅ Total combined summaries prepared: " + summaryList.size());
        return summaryList;
    }

    /**
     * Helper method to fetch GateEntryPackingAndDispatch data using injected repository
     */
    private List<com.indona.invento.entities.GateEntryPackingAndDispatch> fetchGateEntryPackingData() {
        try {
            return gateEntryPackingAndDispatchRepository.findAll();
        } catch (Exception e) {
            System.out.println("   ⚠️ Could not fetch GateEntryPackingAndDispatch data: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Helper method to parse MEDC numbers (can be single string or comma-separated)
     */
    private List<String> parseMedcNumbers(String medcNumber) {
        List<String> medcList = new ArrayList<>();
        if (medcNumber != null && !medcNumber.isBlank()) {
            String[] parts = medcNumber.split("[,;]");
            for (String part : parts) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty()) {
                    medcList.add(trimmed);
                }
            }
        }
        return medcList;
    }

    /**
     * Helper method to parse MEDCI numbers (can be single string or comma-separated)
     */
    private List<String> parseMedciNumbers(String medciNumber) {
        List<String> medciList = new ArrayList<>();
        if (medciNumber != null && !medciNumber.isBlank()) {
            String[] parts = medciNumber.split("[,;]");
            for (String part : parts) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty()) {
                    medciList.add(trimmed);
                }
            }
        }
        return medciList;
    }

}


