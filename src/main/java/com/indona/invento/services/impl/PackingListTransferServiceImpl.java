package com.indona.invento.services.impl;

import com.indona.invento.dao.PackingListTransferRepository;
import com.indona.invento.dao.PackingSubmissionRepository;
import com.indona.invento.dto.PackingListTransferDTO;
import com.indona.invento.entities.PackingListTransferEntity;
import com.indona.invento.entities.PackingSubmission;
import com.indona.invento.services.PackingListTransferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PackingListTransferServiceImpl implements PackingListTransferService {

    @Autowired
    private PackingListTransferRepository repository;

    @Autowired
    private PackingSubmissionRepository packingSubmissionRepository;

    @Override
    public List<PackingListTransferEntity> savePackingList(List<PackingListTransferDTO> dtos) {
        System.out.println("\n╔════════════════════════════════════════════════════════════╗");
        System.out.println("║        📋 savePackingList() CALLED                          ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        System.out.println("   Total DTOs received: " + dtos.size());
        System.out.println("════════════════════════════════════════════════════════════\n");

        // 🔑 Generate SINGLE MEPC number for ALL entries in this batch
        LocalDateTime batchTimestamp = LocalDateTime.now();
        String batchMEPCNumber = "MEPC" +
                batchTimestamp.format(DateTimeFormatter.ofPattern("ddMMyy")) +
                batchTimestamp.format(DateTimeFormatter.ofPattern("mmss"));

        System.out.println("🔑 Generated BATCH MEPC NUMBER: " + batchMEPCNumber);
        System.out.println("   This will be used for ALL entries in this batch\n");

        return dtos.stream().map(dto -> {
            String logPrefix = "📦 [SO: " + dto.getSoNumber() + " | Line: " + dto.getLineNumber() + "]";
            System.out.println(logPrefix + " Processing...");
            System.out.println(logPrefix + " Using MEPC Code: " + batchMEPCNumber);

            // Check if SO+Line combination already exists
            PackingListTransferEntity existingBySOLine = repository.findBySoNumberAndLineNumber(
                    dto.getSoNumber(), dto.getLineNumber());
            if (existingBySOLine != null) {
                System.out.println(logPrefix + " ⚠️  DUPLICATE SO+LINE DETECTED!");
                System.out.println(logPrefix + "     Existing entry found with code: " + existingBySOLine.getPackingListNumber());
                System.out.println(logPrefix + "     Skipping duplicate save\n");
                return existingBySOLine;
            }

            PackingSubmission submission = packingSubmissionRepository
                    .findBySoNumberAndLineNumber(dto.getSoNumber(), dto.getLineNumber());

            String packingStatus = submission != null ? submission.getPackingStatus() : null;

            PackingListTransferEntity entity = PackingListTransferEntity.builder()
                    .timestamp(batchTimestamp)
                    .packingListNumber(batchMEPCNumber)  // ✅ Same MEPC for all entries in batch
                    .soNumber(dto.getSoNumber())
                    .lineNumber(dto.getLineNumber())
                    .unit(dto.getUnit())
                    .customerCode(dto.getCustomerCode())
                    .customerName(dto.getCustomerName())
                    .packingStatus(packingStatus)
                    .orderType(dto.getOrderType())
                    .productCategory(dto.getProductCategory())
                    .itemDescription(dto.getItemDescription())
                    .brand(dto.getBrand())
                    .grade(dto.getGrade())
                    .temper(dto.getTemper())
                    .dimension(dto.getDimension())
                    .quantityKg(dto.getQuantityKg())
                    .uomKg(dto.getUomKg())
                    .quantityNo(dto.getQuantityNo())
                    .uomNo(dto.getUomNo())
                    .transferStatus("Completed")
                    .build();

            System.out.println(logPrefix + " ✅ Creating new entry with MEPC: " + batchMEPCNumber);
            return entity;
        }).map(entity -> {
            try {
                PackingListTransferEntity saved = repository.save(entity);
                System.out.println("📦 [SO: " + saved.getSoNumber() + " | Line: " + saved.getLineNumber() +
                        "] ✅ SAVED with MEPC: " + saved.getPackingListNumber() + "\n");
                return saved;
            } catch (Exception e) {
                System.err.println("📦 [SO: " + entity.getSoNumber() + " | Line: " + entity.getLineNumber() +
                        "] ❌ SAVE ERROR: " + e.getMessage() + "\n");
                throw new RuntimeException("Failed to save packing list: " + e.getMessage(), e);
            }
        }).collect(Collectors.toList());
    }

    @Override
    public List<PackingListTransferEntity> getAllPackingLists() {
        return repository.findAll();
    }
}
