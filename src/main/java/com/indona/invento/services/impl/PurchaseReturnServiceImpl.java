package com.indona.invento.services.impl;

import com.indona.invento.dao.PurchaseReturnRepository;
import com.indona.invento.dto.PurchaseReturnDTO;
import com.indona.invento.entities.PurchaseReturnEntity;
import com.indona.invento.services.PurchaseReturnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class PurchaseReturnServiceImpl implements PurchaseReturnService {

    @Autowired
    private PurchaseReturnRepository repository;

    @Override
    @Transactional
    public PurchaseReturnEntity createPurchaseReturn(PurchaseReturnDTO dto) {

        // Step 1: Generate purchase return number
        String purchaseReturnNumber = generatePurchaseReturnNumber();

        // Step 2: Create purchase return entity
        PurchaseReturnEntity entity = PurchaseReturnEntity.builder()
                .timestamp(LocalDateTime.now())
                .purchaseReturnNumber(purchaseReturnNumber)
                .itemNumber(dto.getItemNumber())
                .unit(dto.getUnit())
                .supplierCode(dto.getSupplierCode())
                .supplierName(dto.getSupplierName())
                .sectionNumber(dto.getSectionNumber())
                .productCategory(dto.getProductCategory())
                .itemDescription(dto.getItemDescription())
                .brand(dto.getBrand())
                .grade(dto.getGrade())
                .temper(dto.getTemper())
                .quantityKg(dto.getQuantityKg())
                .uomKg(dto.getUomKg())
                .quantityNo(dto.getQuantityNo())
                .uomNo(dto.getUomNo())
                .createdBy(dto.getCreatedBy())
                .build();

        return repository.save(entity);
    }

    /**
     * Generate purchase return number in format: MEDB(YY)(MM)(SEQUENCE)
     * Example: MEDB25100001, MEDB25100002, MEDB25110001
     * Sequence keeps increasing across months (no reset)
     */
    private String generatePurchaseReturnNumber() {
        LocalDateTime now = LocalDateTime.now();
        String yearMonth = now.format(DateTimeFormatter.ofPattern("yyMM"));

        // Get the latest purchase return number
        Optional<String> latestNumberOpt = repository.findLatestPurchaseReturnNumber();

        int nextSequence = 1;

        if (latestNumberOpt.isPresent()) {
            String latestNumber = latestNumberOpt.get();
            // Extract sequence from latest number
            // Format: MEDB + YYMM + NNNNNNNN (sequence keeps increasing)
            if (latestNumber != null && latestNumber.length() >= 13) {
                String latestSequence = latestNumber.substring(9); // Extract sequence part
                
                try {
                    nextSequence = Integer.parseInt(latestSequence) + 1;
                } catch (NumberFormatException e) {
                    nextSequence = 1;
                }
            }
        }

        // Format: MEDB + YYMM + 00000001 (8-digit sequence)
        return String.format("MEDB%s%08d", yearMonth, nextSequence);
    }

    @Override
    public List<PurchaseReturnEntity> getAllPurchaseReturns() {
        return repository.findAll();
    }

    @Override
    public PurchaseReturnEntity getPurchaseReturnById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Purchase return not found with id: " + id));
    }
}

