package com.indona.invento.services.impl;

import com.indona.invento.dao.BlockedQuantityRepository;
import com.indona.invento.dao.ItemEnquiryRepository;
import com.indona.invento.dto.BlockedQuantityRequest;
import com.indona.invento.entities.BlockedProductEntity;
import com.indona.invento.entities.BlockedQuantityEntity;
import com.indona.invento.entities.ItemEnquiry;
import com.indona.invento.services.BlockedQuantityService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlockedQuantityServiceImpl implements BlockedQuantityService {

    private final BlockedQuantityRepository blockedQuantityRepository;

    @Autowired
    private ItemEnquiryRepository itemEnquiryRepository;

    @Override
    @Transactional
    public BlockedQuantityEntity createBlockedQuantity(BlockedQuantityRequest request) {
        // Step 1: Create BlockedQuantity
        BlockedQuantityEntity blockedQuantity = BlockedQuantityEntity.builder()
                .quotationNo(request.getQuotationNo())
                .pdfLink(request.getPdflink())
                .customerName(request.getCustomerName())
                .marketingExecutiveName(request.getMarketingExecutiveName())
                .build();

        List<BlockedProductEntity> products = request.getProducts().stream()
                .map(p -> BlockedProductEntity.builder()
                        .itemDescription(p.getItemDescription())
                        .availableQuantityKg(p.getAvailableQuantityKg())
                        .blockedQuantity(blockedQuantity)
                        .build())
                .toList();

        blockedQuantity.setProducts(products);

        BlockedQuantityEntity savedBlockedQuantity = blockedQuantityRepository.save(blockedQuantity);
        log.info("BlockedQuantity saved with id={} and quotationNo={}",
                savedBlockedQuantity.getId(), savedBlockedQuantity.getQuotationNo());

        // Step 2: Update ItemEnquiry for same quotationNo
        ItemEnquiry enquiry = itemEnquiryRepository.findByQuotationNo(request.getQuotationNo())
                .orElseThrow(() -> new RuntimeException("ItemEnquiry not found for quotationNo: " + request.getQuotationNo()));

        log.info("Before update: ItemEnquiry id={}, quotationNo={}, blockedPdfLink={}, isBlocked={}, pdfLink={}",
                enquiry.getId(), enquiry.getQuotationNo(), enquiry.getBlockedPdflinked(), enquiry.getIsLocked(), enquiry.getPdfLink());

        // ✅ अब blockedPdfLink में वही link डालें और isBlocked true करें
        enquiry.setBlockedPdflinked(request.getPdflink());
        enquiry.setIsLocked(true);

        ItemEnquiry updatedEnquiry = itemEnquiryRepository.save(enquiry);

        log.info("After update: ItemEnquiry id={}, quotationNo={}, blockedPdfLink={}, isBlocked={}, pdfLink={}",
                updatedEnquiry.getId(), updatedEnquiry.getQuotationNo(), updatedEnquiry.getBlockedPdflinked(), updatedEnquiry.getIsLocked(), updatedEnquiry.getPdfLink());

        return savedBlockedQuantity;
    }

    @Scheduled(fixedRate = 3600000)
    public void deleteOldBlockedQuantities() {
        Date cutoff = new Date(System.currentTimeMillis() - (48 * 60 * 60 * 1000)); // 48 hours ago
        List<BlockedQuantityEntity> oldRecords = blockedQuantityRepository.findByCreatedAtBefore(cutoff);

        if (!oldRecords.isEmpty()) {
            blockedQuantityRepository.deleteAll(oldRecords);
            System.out.println("Deleted " + oldRecords.size() + " blocked quantity records older than 48 hours.");
        }
    }
}

