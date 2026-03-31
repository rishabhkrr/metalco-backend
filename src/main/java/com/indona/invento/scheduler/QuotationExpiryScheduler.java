package com.indona.invento.scheduler;

import com.indona.invento.dao.ItemEnquiryRepository;
import com.indona.invento.dao.BlockedQuantityRepository;
import com.indona.invento.entities.ItemEnquiry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * FRD BLK-004: Auto-invalidation scheduler for quotations.
 * 
 * Runs every 15 minutes to check for expired quotations:
 * - Quotation validity = 48 hours from creation date
 * - If 48 hours have passed and status is "Pending" or "SO PENDING", 
 *   the status changes to "INVALID" and blocked quantities are released.
 */
@Component
@Slf4j
public class QuotationExpiryScheduler {

    @Autowired
    private ItemEnquiryRepository enquiryRepo;

    @Autowired
    private BlockedQuantityRepository blockedQuantityRepo;

    /**
     * Runs every 15 minutes to check for expired quotations.
     * Quotations older than 48 hours with active statuses are auto-invalidated.
     */
    @Scheduled(fixedRate = 900000) // 15 minutes in milliseconds
    @Transactional
    public void invalidateExpiredQuotations() {
        LocalDateTime expiryThreshold = LocalDateTime.now().minusHours(48);

        log.info("┌─ QUOTATION EXPIRY SCHEDULER ─────────────────────────────────────┐");
        log.info("│ Checking for quotations created before: {}", expiryThreshold);

        // Find all quotations older than 48 hours with active statuses
        List<ItemEnquiry> expiredQuotations = enquiryRepo.findByCreatedAtBeforeAndStatusIn(
                expiryThreshold,
                List.of("Pending", "SO PENDING", "PENDING_CUSTOMER_VERIFICATION")
        );

        if (expiredQuotations.isEmpty()) {
            log.info("│ ✅ No expired quotations found");
            log.info("└──────────────────────────────────────────────────────────────────┘");
            return;
        }

        log.info("│ Found {} expired quotations to invalidate", expiredQuotations.size());

        int invalidatedCount = 0;
        int releasedBlockCount = 0;

        for (ItemEnquiry enquiry : expiredQuotations) {
            String previousStatus = enquiry.getStatus();
            enquiry.setStatus("INVALID");
            enquiryRepo.save(enquiry);
            invalidatedCount++;

            log.info("│ ❌ Quotation {} (ID={}) expired: {} → INVALID", 
                    enquiry.getQuotationNo(), enquiry.getId(), previousStatus);

            // Release blocked quantity if any
            blockedQuantityRepo.findByQuotationNo(enquiry.getQuotationNo()).ifPresent(blocked -> {
                blockedQuantityRepo.delete(blocked);
                log.info("│   🔓 Released blocked quantity for: {}", enquiry.getQuotationNo());
            });
            releasedBlockCount++;
        }

        log.info("│ Summary: {} quotations invalidated, {} block checks processed", 
                invalidatedCount, releasedBlockCount);
        log.info("└──────────────────────────────────────────────────────────────────┘");
    }
}
