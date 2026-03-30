package com.indona.invento.services.impl;


import com.indona.invento.dao.PurchaseFollowUpV2Repository;
import com.indona.invento.dto.PurchaseFollowUpUpdateDTO;
import com.indona.invento.entities.PurchaseFollowUpEntityV2;
import com.indona.invento.services.PurchaseFollowUpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PurchaseFollowUpServiceImpl implements PurchaseFollowUpService {

    @Autowired
    private PurchaseFollowUpV2Repository repository;

    @Override
    public boolean markFollowUpCompleted(PurchaseFollowUpUpdateDTO dto) {
        Optional<PurchaseFollowUpEntityV2> optional = repository
                .findByPoNumberAndSalesOrderNumber(dto.getPoNumber(), dto.getSalesOrderNumber());

        if (optional.isEmpty()) return false;

        PurchaseFollowUpEntityV2 entity = optional.get();
        entity.setFollowUpStatus("Completed");
        repository.save(entity);
        return true;
    }

    @Override
    public void deleteAllFollowUps() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║  🗑️  DELETE ALL PURCHASE FOLLOW-UPS   ║");
        System.out.println("╚════════════════════════════════════════╝\n");

        try {
            long totalCount = repository.count();
            System.out.println("📊 Total follow-ups before deletion: " + totalCount);

            repository.deleteAll();

            long afterCount = repository.count();
            System.out.println("✅ All follow-ups deleted successfully!");
            System.out.println("📊 Total follow-ups after deletion: " + afterCount);
            System.out.println("\n╔════════════════════════════════════════╗");
            System.out.println("║     ✅ DELETION COMPLETE               ║");
            System.out.println("╚════════════════════════════════════════╝\n");
        } catch (Exception e) {
            System.err.println("❌ Error deleting all follow-ups: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to delete all follow-ups: " + e.getMessage());
        }
    }
}
