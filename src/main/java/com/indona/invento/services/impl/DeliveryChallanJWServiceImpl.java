package com.indona.invento.services.impl;

import com.indona.invento.dao.DeliveryChallanJWRepository;
import com.indona.invento.dao.GateInwardRepository;
import com.indona.invento.dao.VehicleWeighmentRepository;
import com.indona.invento.dto.DeliveryChallanItemDetailsDTO;
import com.indona.invento.dto.DeliveryChallanMergedDTO;
import com.indona.invento.entities.DeliveryChallanJWEntity;
import com.indona.invento.entities.GateInwardEntity;
import com.indona.invento.entities.VehicleWeighmentEntity;
import com.indona.invento.services.DeliveryChallanJWService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DeliveryChallanJWServiceImpl implements DeliveryChallanJWService {

    @Autowired
    private DeliveryChallanJWRepository repository;
    
    @Autowired
    private GateInwardRepository gateInwardRepository;

    @Autowired
    private VehicleWeighmentRepository vehicleWeighmentRepository;    @Override
    public List<DeliveryChallanJWEntity> saveAll(List<DeliveryChallanJWEntity> challans) {
        log.info("Saving multiple Delivery Challan JW records: {}", challans.size());
        return challans.stream()
                .map(this::save)
                .collect(Collectors.toList());
    }

    @Override
    public DeliveryChallanJWEntity save(DeliveryChallanJWEntity challan) {
        log.info("Saving single Delivery Challan JW...");

        LocalDateTime now = LocalDateTime.now();
        challan.setTimestamp(now);

        // ✅ Generate MEDC number (MEDC + YYMM + 0001)
        String prefix = "MEDC" + now.format(DateTimeFormatter.ofPattern("yyMM"));
        String lastMEDC = repository.findLatestMedcNumberLike(prefix + "%");

        int nextCounter = 1;
        if (lastMEDC != null && lastMEDC.length() >= 12) {
            try {
                // lastMEDC format assumed like MEDCYYMMxxxx -> counter starts at position 8 (0-based)
                nextCounter = Integer.parseInt(lastMEDC.substring(8)) + 1;
            } catch (NumberFormatException e) {
                log.warn("Failed to parse counter from MEDC Number: {}", lastMEDC);
            }
        }

        String newMEDCNumber = prefix + String.format("%04d", nextCounter);
        challan.setMedcNumber(newMEDCNumber);

        return repository.save(challan);
    }

    @Override
    public List<DeliveryChallanJWEntity> getAll() {
        log.info("Fetching all Delivery Challan JW records...");
        return repository.findAll();
    }

    @Override
    public DeliveryChallanJWEntity getByMedcNumber(String medcNumber) {
        log.info("Fetching Delivery Challan JW by MEDC Number: {}", medcNumber);
        return repository.findByMedcNumber(medcNumber);
    }
    
    @Override
    public DeliveryChallanJWEntity updateById(Long id, DeliveryChallanJWEntity updatedChallan) {
        log.info("Updating delivery challan with id: {}", id);
        return repository.findById(id)
                .map(existing -> {
                    updatedChallan.setId(existing.getId()); // ensure same ID
                    return repository.save(updatedChallan);
                })
                .orElseThrow(() -> new RuntimeException("Delivery Challan not found with id: " + id));
    }

    @Override
    public void deleteById(Long id) {
        log.info("Deleting delivery challan with id: {}", id);
        if (!repository.existsById(id)) {
            throw new RuntimeException("Delivery Challan not found with id: " + id);
        }
        repository.deleteById(id);
    }
    
    @Override
    public DeliveryChallanJWEntity getById(Long id) {
        log.info("Fetching delivery challan by id: {}", id);
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Delivery Challan not found with id: " + id));
    }
    
    @Override
    public List<String> getAllMedcNumbers() {
        return repository.findAllMedcNumbers();
    }
    
    @Override
    public List<DeliveryChallanMergedDTO> getMergedDataByMedcNumbers(List<String> medcNumbers) {
        List<DeliveryChallanJWEntity> challans = repository.findByMedcNumberIn(medcNumbers);
        List<GateInwardEntity> gateInwards = gateInwardRepository.findByDcNumberIn(medcNumbers);
        // Get all weighments (filtering by medcNumbers will happen in loop below)
        List<VehicleWeighmentEntity> weighments = vehicleWeighmentRepository.findAll();

        Map<String, GateInwardEntity> gateMap = gateInwards.stream()
                .collect(Collectors.toMap(GateInwardEntity::getDcNumber, g -> g, (a, b) -> a));

        List<DeliveryChallanMergedDTO> result = new ArrayList<>();

        for (DeliveryChallanJWEntity challan : challans) {
            DeliveryChallanMergedDTO dto = new DeliveryChallanMergedDTO();
            dto.setMedcNumber(challan.getMedcNumber());
            dto.setTimestamp(challan.getTimestamp());
            dto.setUnit(challan.getUnit());
            dto.setSubContractorCode(challan.getSubContractorCode());
            dto.setSubContractorName(challan.getSubContractorName());

            GateInwardEntity gate = gateMap.get(challan.getMedcNumber());
            if (gate != null) {
                dto.setGatePassRefNumber(gate.getGatePassRefNumber());
                dto.setEWayBillNumber(gate.getEWayBillNumber());
                dto.setVehicleNumber(gate.getVehicleNumber());
                dto.setInvoiceScanUrls(gate.getInvoiceScanUrls());
                dto.setDcDocumentScanUrls(gate.getDcDocumentScanUrls());
                dto.setEWayBillScanUrls(gate.getEWayBillScanUrls());
                dto.setVehicleDocumentsScanUrls(gate.getVehicleDocumentsScanUrls());
            }

            // Find weighment by any matching MEDC number or weightment ref
            VehicleWeighmentEntity weigh = weighments.stream()
                    .filter(w -> w.getWeightmentRefNumber() != null)
                    .findFirst()
                    .orElse(null);

            if (weigh != null) {
                dto.setWeightmentRefNumber(weigh.getWeightmentRefNumber());
                dto.setLoadWeight(weigh.getLoadWeight());
                dto.setEmptyWeight(weigh.getEmptyWeight());
            }

            result.add(dto);
        }

        return result;
    }
    
    @Override
    public List<DeliveryChallanItemDetailsDTO> getItemDetailsByMedcNumber(String medcNumber) {
        List<DeliveryChallanJWEntity> entities = repository.findAllByMedcNumber(medcNumber);

        if (entities == null || entities.isEmpty()) {
            throw new RuntimeException("No Delivery Challan items found for MEDC Number: " + medcNumber);
        }

        return entities.stream().map(entity -> DeliveryChallanItemDetailsDTO.builder()
                .itemDescription(entity.getItemDescription())
                .orderType(entity.getOrderType())
                .productCategory(entity.getProductCategory())
                .brand(entity.getBrand())
                .grade(entity.getGrade())
                .temper(entity.getTemper())
                .dimension(entity.getDimension())
                .quantityKg(entity.getQuantityKg())
                .uomKg(entity.getUomKg())
                .quantityNo(entity.getQuantityNo())
                .uomNo(entity.getUomNo())
                .build()
        ).collect(Collectors.toList());
    }
    
    public List<String> getDimensionsByMedcNumber(String medcNumber) {
        return repository.findDimensionsByMedcNumber(medcNumber);
    }
    
    @Override
    public List<DeliveryChallanItemDetailsDTO> getItemDetailsByMedcAndDimension(String medcNumber, String dimension) {
        return repository.findItemDetailsByMedcAndDimension(medcNumber, dimension);
    }

    @Override
    public void deleteAll() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║  🗑️  DELETE ALL DELIVERY CHALLAN JW   ║");
        System.out.println("╚════════════════════════════════════════╝\n");

        try {
            long totalCount = repository.count();
            System.out.println("📊 Total delivery challans before deletion: " + totalCount);

            repository.deleteAll();

            long afterCount = repository.count();
            System.out.println("✅ All delivery challans deleted successfully!");
            System.out.println("📊 Total delivery challans after deletion: " + afterCount);
            System.out.println("\n╔════════════════════════════════════════╗");
            System.out.println("║     ✅ DELETION COMPLETE               ║");
            System.out.println("╚════════════════════════════════════════╝\n");
        } catch (Exception e) {
            System.err.println("❌ Error deleting all delivery challans: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to delete all delivery challans: " + e.getMessage());
        }
    }

}