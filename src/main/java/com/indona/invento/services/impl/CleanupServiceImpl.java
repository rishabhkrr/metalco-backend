package com.indona.invento.services.impl;

import com.indona.invento.dao.*;
import com.indona.invento.services.CleanupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
public class CleanupServiceImpl implements CleanupService {

    @Autowired private ItemEnquiryRepository itemEnquiryRepository;
    @Autowired private SalesOrderRepository salesOrderRepository;
    @Autowired private SalesOrderSchedulerRepository salesOrderSchedulerRepository;
    @Autowired private StockSummaryRepository stockSummaryRepository;
    @Autowired private PurchaseFollowUpRepository purchaseFollowUpRepository;
    @Autowired private POGenerationRepository poGenerationRepository;
    @Autowired private MaterialRequestSummaryHeaderRepository materialRequestSummaryHeaderRepository;
    @Autowired private GateInwardRepository gateInwardRepository;
    @Autowired private VehicleWeighmentRepository vehicleWeighmentRepository;
    @Autowired private GRNRepository grnRepository;
    @Autowired private StockTransferRepository stockTransferRepository;
    @Autowired private GRNJobWorkRepository grnJobWorkRepository;
    @Autowired private PackingSchedulerRepository packingSchedulerRepository;
    @Autowired private PackingSubmissionRepository packingSubmissionRepository;
    @Autowired private BillingSummaryRepository billingSummaryRepository;
    @Autowired private SoSummaryRepository soSummaryRepository;
    @Autowired private GateEntryPackingAndDispatchRepository gateEntryPackingAndDispatchRepository;
    @Autowired private DeliveryChallanCreationIUMTRepository deliveryChallanCreationIUMTRepository;
    @Autowired private CreditDebitNoteRepository creditDebitNoteRepository;
    @Autowired private PurchaseCreditDebitNoteRepository purchaseCreditDebitNoteRepository;
    @Autowired private ProductionEntryRepository productionEntryRepository;
    @Autowired private ProductionIdleTimeEntryRepository productionIdleTimeEntryRepository;
    @Autowired private MachineMaintenanceActivityRepository machineMaintenanceActivityRepository;

    @Override
    @Transactional
    public Map<String, Object> clearAllData() {
        log.info("\n╔═══════════════════════════════════════════════════════════════════╗");
        log.info("║  🗑️ STARTING SYSTEM-WIDE DATA CLEANUP - CLEARING ALL RECORDS 🗑️  ║");
        log.info("╚═══════════════════════════════════════════════════════════════════╝\n");

        Map<String, Object> results = new LinkedHashMap<>();
        long startTime = System.currentTimeMillis();

        try {
            // 1. Item Enquiries
            long count1 = itemEnquiryRepository.count();
            itemEnquiryRepository.deleteAll();
            results.put("Item Enquiries Deleted", count1);
            log.info("✅ Item Enquiries: {} records deleted", count1);

            // 2. Sales Orders
            long count2 = salesOrderRepository.count();
            salesOrderRepository.deleteAll();
            results.put("Sales Orders Deleted", count2);
            log.info("✅ Sales Orders: {} records deleted", count2);

            // 3. Sales Order Scheduler
            long count3 = salesOrderSchedulerRepository.count();
            salesOrderSchedulerRepository.deleteAll();
            results.put("Sales Order Scheduler Deleted", count3);
            log.info("✅ Sales Order Scheduler: {} records deleted", count3);

            // 4. Stock Summary
            long count4 = stockSummaryRepository.count();
            stockSummaryRepository.deleteAll();
            results.put("Stock Summary Deleted", count4);
            log.info("✅ Stock Summary: {} records deleted", count4);

            // 5. Purchase Follow Up
            long count5 = purchaseFollowUpRepository.count();
            purchaseFollowUpRepository.deleteAll();
            results.put("Purchase Follow Up Deleted", count5);
            log.info("✅ Purchase Follow Up: {} records deleted", count5);

            // 6. PO Generation
            long count6 = poGenerationRepository.count();
            poGenerationRepository.deleteAll();
            results.put("PO Generation Deleted", count6);
            log.info("✅ PO Generation: {} records deleted", count6);

            // 7. Material Request Summary
            long count7 = materialRequestSummaryHeaderRepository.count();
            materialRequestSummaryHeaderRepository.deleteAll();
            results.put("Material Request Summary Deleted", count7);
            log.info("✅ Material Request Summary: {} records deleted", count7);

            // 8. Gate Inward
            long count8 = gateInwardRepository.count();
            gateInwardRepository.deleteAll();
            results.put("Gate Inward Deleted", count8);
            log.info("✅ Gate Inward: {} records deleted", count8);

            // 9. Vehicle Weighment
            long count9 = vehicleWeighmentRepository.count();
            vehicleWeighmentRepository.deleteAll();
            results.put("Vehicle Weighment Deleted", count9);
            log.info("✅ Vehicle Weighment: {} records deleted", count9);

            // 10. GRN Summary
            long count10 = grnRepository.count();
            grnRepository.deleteAll();
            results.put("GRN Summary Deleted", count10);
            log.info("✅ GRN Summary: {} records deleted", count10);

            // 11. Stock Transfer
            long count11 = stockTransferRepository.count();
            stockTransferRepository.deleteAll();
            results.put("Stock Transfer Deleted", count11);
            log.info("✅ Stock Transfer: {} records deleted", count11);

            // 12. GRN Job Work
            long count12 = grnJobWorkRepository.count();
            grnJobWorkRepository.deleteAll();
            results.put("GRN Job Work Deleted", count12);
            log.info("✅ GRN Job Work: {} records deleted", count12);

            // 13. Packing Scheduler
            long count13 = packingSchedulerRepository.count();
            packingSchedulerRepository.deleteAll();
            results.put("Packing Scheduler Deleted", count13);
            log.info("✅ Packing Scheduler: {} records deleted", count13);

            // 14. Packing Submission
            long count14 = packingSubmissionRepository.count();
            packingSubmissionRepository.deleteAll();
            results.put("Packing Submission Deleted", count14);
            log.info("✅ Packing Submission: {} records deleted", count14);

            // 15. Billing Summary
            long count15 = billingSummaryRepository.count();
            billingSummaryRepository.deleteAll();
            results.put("Billing Summary Deleted", count15);
            log.info("✅ Billing Summary: {} records deleted", count15);

            // 16. SO Summary
            long count16 = soSummaryRepository.count();
            soSummaryRepository.deleteAll();
            results.put("SO Summary Deleted", count16);
            log.info("✅ SO Summary: {} records deleted", count16);

            // 17. Gate Entry Packing and Dispatch
            long count17 = gateEntryPackingAndDispatchRepository.count();
            gateEntryPackingAndDispatchRepository.deleteAll();
            results.put("Gate Entry Packing Dispatch Deleted", count17);
            log.info("✅ Gate Entry Packing Dispatch: {} records deleted", count17);

            // 18. Delivery Challan IUMT
            long count18 = deliveryChallanCreationIUMTRepository.count();
            deliveryChallanCreationIUMTRepository.deleteAll();
            results.put("Delivery Challan IUMT Deleted", count18);
            log.info("✅ Delivery Challan IUMT: {} records deleted", count18);

            // 19. Credit Debit Note
            long count19 = creditDebitNoteRepository.count();
            creditDebitNoteRepository.deleteAll();
            results.put("Credit Debit Note Deleted", count19);
            log.info("✅ Credit Debit Note: {} records deleted", count19);

            // 20. Purchase Credit Debit Note
            long count20 = purchaseCreditDebitNoteRepository.count();
            purchaseCreditDebitNoteRepository.deleteAll();
            results.put("Purchase Credit Debit Note Deleted", count20);
            log.info("✅ Purchase Credit Debit Note: {} records deleted", count20);

            // 21. Production Entry
            long count21 = productionEntryRepository.count();
            productionEntryRepository.deleteAll();
            results.put("Production Entry Deleted", count21);
            log.info("✅ Production Entry: {} records deleted", count21);

            // 22. Production Idle Time Entry
            long count22 = productionIdleTimeEntryRepository.count();
            productionIdleTimeEntryRepository.deleteAll();
            results.put("Production Idle Time Entry Deleted", count22);
            log.info("✅ Production Idle Time Entry: {} records deleted", count22);

            // 23. Machine Maintenance Activity
            long count23 = machineMaintenanceActivityRepository.count();
            machineMaintenanceActivityRepository.deleteAll();
            results.put("Machine Maintenance Activity Deleted", count23);
            log.info("✅ Machine Maintenance Activity: {} records deleted", count23);

            long totalTime = System.currentTimeMillis() - startTime;
            long totalDeleted = results.values().stream()
                    .filter(v -> v instanceof Long)
                    .mapToLong(v -> (Long) v)
                    .sum();

            results.put("Total Records Deleted", totalDeleted);
            results.put("Execution Time (ms)", totalTime);
            results.put("Status", "✅ CLEANUP COMPLETE");

            log.info("\n╔═══════════════════════════════════════════════════════════════════╗");
            log.info("║  ✅ CLEANUP COMPLETE - SYSTEM RESET SUCCESSFUL ✅                 ║");
            log.info("║  Total Records Deleted: {}                                     ", totalDeleted);
            log.info("║  Time Taken: {} ms                                          ", totalTime);
            log.info("╚═══════════════════════════════════════════════════════════════════╝\n");

            return results;

        } catch (Exception e) {
            log.error("❌ CLEANUP FAILED: {}", e.getMessage(), e);
            results.put("Status", "❌ CLEANUP FAILED");
            results.put("Error", e.getMessage());
            return results;
        }
    }
}

