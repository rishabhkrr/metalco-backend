package com.indona.invento.services.impl;

import com.indona.invento.dao.SoUpdateRepository;
import com.indona.invento.entities.SoUpdate;
import com.indona.invento.services.SoUpdateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SoUpdateServiceImpl implements SoUpdateService {

    private final SoUpdateRepository soUpdateRepository;

    @Override
    public List<SoUpdate> getAllSoUpdates() {
        return soUpdateRepository.findAll();
    }

//    @Override
//    public List<SoUpdate> getAllSoUpdates() {
//
//        List<SoUpdate> updates = soUpdateRepository.findAll();
//
//        updates.forEach(update -> {
//            if ("Pending".equalsIgnoreCase(update.getStatus())) {
//                update.setStatus("Hold");
//            }
//        });
//
//        return updates;
//    }

    @Override
    public SoUpdate verifyStatus(String soNumber) {
        SoUpdate update = soUpdateRepository.findBySoNumber(soNumber)
                .orElseThrow(() -> new RuntimeException("SO Update not found with number: " + soNumber));

        update.setStatus("Verified");
        return soUpdateRepository.save(update);
    }

    @Override
    public SoUpdate save(SoUpdate soUpdate) {
        return soUpdateRepository.save(soUpdate);
    }

    @Override
    public void deleteAllSoUpdates() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║     🗑️  DELETE ALL SO UPDATES          ║");
        System.out.println("╚════════════════════════════════════════╝\n");

        try {
            long totalCount = soUpdateRepository.count();
            System.out.println("📊 Total SO updates before deletion: " + totalCount);

            soUpdateRepository.deleteAll();

            long afterCount = soUpdateRepository.count();
            System.out.println("✅ All SO updates deleted successfully!");
            System.out.println("📊 Total SO updates after deletion: " + afterCount);
            System.out.println("\n╔════════════════════════════════════════╗");
            System.out.println("║     ✅ DELETION COMPLETE               ║");
            System.out.println("╚════════════════════════════════════════╝\n");
        } catch (Exception e) {
            System.err.println("❌ Error deleting all SO updates: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to delete all SO updates: " + e.getMessage());
        }
    }
}
