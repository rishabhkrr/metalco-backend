package com.indona.invento.services.impl;

import com.indona.invento.dao.RawMaterialQrRepository;
import com.indona.invento.dto.RawMaterialQrDTO;
import com.indona.invento.entities.RawMaterialQrEntity;
import com.indona.invento.services.AuditLogService;
import com.indona.invento.services.RawMaterialQrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class RawMaterialQrServiceImpl implements RawMaterialQrService {

    @Autowired
    private RawMaterialQrRepository rawMaterialQrRepository;

    @Autowired
    private AuditLogService auditLogService;

    /**
     * FRD Module 7 — QR Code Format:
     * UNIT-STORE-AREA-RACK-COLUMN-BIN-BATCHNO-ITEMCODE-SLNO
     * Example: U01-S01-A01-R01-C01-B01-BATCH001-ALU001-SL001
     */
    @Override
    @Transactional
    public RawMaterialQrEntity createRawMaterialQr(RawMaterialQrDTO dto) {

        // Build entity
        RawMaterialQrEntity entity = RawMaterialQrEntity.builder()
                .slNo(dto.getSlNo())
                .grnNumber(dto.getGrnNumber())
                .itemDescription(dto.getItemDescription())
                .productCategory(dto.getProductCategory())
                .sectionNumber(dto.getSectionNumber())
                .brand(dto.getBrand())
                .grade(dto.getGrade())
                .temper(dto.getTemper())
                .dimension(dto.getDimension())
                .store(dto.getStore())
                .storageArea(dto.getStorageArea())
                .rackNo(dto.getRackNo())
                .columnNo(dto.getColumnNo())
                .binNo(dto.getBinNo())
                .rackColumnBin(dto.getRackColumnBin())
                .quantityKg(dto.getQuantityKg())
                .quantityNo(dto.getQuantityNo())
                .batchNumber(dto.getBatchNumber())
                .unitCode(dto.getUnitCode())
                .itemCode(dto.getItemCode())
                .locationStatus("IN_STOCK")
                .build();

        // Save to get auto-generated id
        RawMaterialQrEntity savedEntity = rawMaterialQrRepository.save(entity);

        // Generate FRD-compliant QR ID:
        // Format: UNIT-STORE-AREA-RACK-COLUMN-BIN-BATCHNO-ITEMCODE-SLNO
        String rawMaterialQrId = generateFrdCompliantQrId(savedEntity);
        savedEntity.setRawMaterialQrId(rawMaterialQrId);

        // Also set rackColumnBin composite
        String rackColumnBin = sanitize(dto.getRackNo()) + "-" +
                               sanitize(dto.getColumnNo()) + "-" +
                               sanitize(dto.getBinNo());
        savedEntity.setRackColumnBin(rackColumnBin);

        RawMaterialQrEntity result = rawMaterialQrRepository.save(savedEntity);

        // Audit log
        auditLogService.logAction("CREATE", "QR_CODE", "RawMaterialQr",
                result.getId(), result.getRawMaterialQrId(), null, "IN_STOCK",
                "QR Code generated for batch " + dto.getBatchNumber() +
                        " item " + dto.getItemDescription(),
                "SYSTEM", dto.getUnitCode());

        return result;
    }

    /**
     * Generates QR code ID following FRD specification:
     * UNIT-STORE-AREA-RACK-COLUMN-BIN-BATCHNO-ITEMCODE-SLNO
     */
    private String generateFrdCompliantQrId(RawMaterialQrEntity entity) {
        StringBuilder qrId = new StringBuilder();
        qrId.append(sanitize(entity.getUnitCode()));
        qrId.append("-").append(sanitize(entity.getStore()));
        qrId.append("-").append(sanitize(entity.getStorageArea()));
        qrId.append("-").append(sanitize(entity.getRackNo()));
        qrId.append("-").append(sanitize(entity.getColumnNo()));
        qrId.append("-").append(sanitize(entity.getBinNo()));
        qrId.append("-").append(sanitize(entity.getBatchNumber()));
        qrId.append("-").append(sanitize(entity.getItemCode()));
        qrId.append("-SL").append(String.format("%03d", entity.getId()));
        return qrId.toString();
    }

    private String sanitize(String value) {
        if (value == null || value.trim().isEmpty()) return "NA";
        return value.trim().replaceAll("[^A-Za-z0-9]", "");
    }

    @Override
    public RawMaterialQrEntity getByRawMaterialQrId(String rawMaterialQrId) {
        return rawMaterialQrRepository.findByRawMaterialQrId(rawMaterialQrId)
                .orElseThrow(() -> new RuntimeException("Raw Material QR not found with id: " + rawMaterialQrId));
    }

    /**
     * Decode QR code string back into its components.
     * Format: UNIT-STORE-AREA-RACK-COLUMN-BIN-BATCHNO-ITEMCODE-SLNO
     */
    public Map<String, String> decodeQrCode(String qrContent) {
        Map<String, String> decoded = new HashMap<>();
        if (qrContent == null || qrContent.isEmpty()) {
            throw new RuntimeException("QR code content is empty");
        }

        String[] parts = qrContent.split("-");
        if (parts.length >= 9) {
            decoded.put("unitCode", parts[0]);
            decoded.put("store", parts[1]);
            decoded.put("storageArea", parts[2]);
            decoded.put("rackNo", parts[3]);
            decoded.put("columnNo", parts[4]);
            decoded.put("binNo", parts[5]);
            decoded.put("batchNumber", parts[6]);
            decoded.put("itemCode", parts[7]);
            decoded.put("slNo", parts[8]);
        } else {
            // Legacy format: RMQC-BATCHNO-ID
            decoded.put("rawMaterialQrId", qrContent);
            decoded.put("format", "legacy");
        }

        return decoded;
    }

    /**
     * Validate QR scan — returns item details and current location.
     * Used during stock movements, packing, and gate operations.
     */
    public Map<String, Object> validateQrScan(String qrContent) {
        Map<String, Object> result = new HashMap<>();

        // Try to find by exact QR ID first
        try {
            RawMaterialQrEntity entity = rawMaterialQrRepository.findByRawMaterialQrId(qrContent)
                    .orElse(null);

            if (entity != null) {
                result.put("valid", true);
                result.put("qrId", entity.getRawMaterialQrId());
                result.put("batchNumber", entity.getBatchNumber());
                result.put("itemDescription", entity.getItemDescription());
                result.put("itemCode", entity.getItemCode());
                result.put("grade", entity.getGrade());
                result.put("dimension", entity.getDimension());
                result.put("quantityKg", entity.getQuantityKg());
                result.put("quantityNo", entity.getQuantityNo());
                result.put("currentStore", entity.getStore());
                result.put("currentRack", entity.getRackNo());
                result.put("currentBin", entity.getBinNo());
                result.put("locationStatus", entity.getLocationStatus());
                result.put("unitCode", entity.getUnitCode());
                result.put("grnNumber", entity.getGrnNumber());
                result.put("createdAt", entity.getCreatedAt());
            } else {
                result.put("valid", false);
                result.put("error", "QR code not found in system");
            }
        } catch (Exception e) {
            result.put("valid", false);
            result.put("error", e.getMessage());
        }

        return result;
    }

    /**
     * Update QR location after stock transfer.
     */
    @Transactional
    public RawMaterialQrEntity updateQrLocation(String rawMaterialQrId, String newStore,
                                                  String newRack, String newColumn,
                                                  String newBin, String newStatus) {
        RawMaterialQrEntity entity = rawMaterialQrRepository.findByRawMaterialQrId(rawMaterialQrId)
                .orElseThrow(() -> new RuntimeException("QR not found: " + rawMaterialQrId));

        String previousLocation = entity.getStore() + "-" + entity.getRackNo() + "-" +
                entity.getColumnNo() + "-" + entity.getBinNo();

        entity.setStore(newStore);
        entity.setRackNo(newRack);
        entity.setColumnNo(newColumn);
        entity.setBinNo(newBin);
        if (newStatus != null) {
            entity.setLocationStatus(newStatus);
        }

        // Regenerate QR ID with new location
        entity.setRawMaterialQrId(generateFrdCompliantQrId(entity));

        RawMaterialQrEntity updated = rawMaterialQrRepository.save(entity);

        String newLocation = newStore + "-" + newRack + "-" + newColumn + "-" + newBin;

        auditLogService.logAction("STATUS_CHANGE", "QR_CODE", "RawMaterialQr",
                entity.getId(), entity.getRawMaterialQrId(), previousLocation, newLocation,
                "QR location updated from " + previousLocation + " to " + newLocation,
                "SYSTEM", entity.getUnitCode());

        return updated;
    }
}
