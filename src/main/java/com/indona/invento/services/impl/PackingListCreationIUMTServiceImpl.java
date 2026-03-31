package com.indona.invento.services.impl;

import com.indona.invento.dao.DeliveryChallanCreationIUMTRepository;
import com.indona.invento.dao.MaterialRequestSummaryHeaderRepository;
import com.indona.invento.dao.PackingListCreationIUMTRepository;
import com.indona.invento.entities.MaterialRequestSummaryHeader;
import com.indona.invento.entities.MaterialRequestSummaryItem;
import com.indona.invento.entities.PackingListCreationIUMTEntity;
import com.indona.invento.services.PackingListCreationIUMTService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PackingListCreationIUMTServiceImpl implements PackingListCreationIUMTService {

    @Autowired
    private PackingListCreationIUMTRepository packingListCreationIUMTRepository;

    @Autowired
    private MaterialRequestSummaryHeaderRepository materialRequestSummaryHeaderRepository;

    @Autowired
    private DeliveryChallanCreationIUMTRepository deliveryChallanRepository;

    private static final DateTimeFormatter PL_NO_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    private static final ZoneId KOLKATA = ZoneId.of("Asia/Kolkata");

    @Override
    @Transactional
    public List<PackingListCreationIUMTEntity> createPackingListIUMT(List<PackingListCreationIUMTEntity> toCreate) {
        if (toCreate == null || toCreate.isEmpty()) {
            return List.of();
        }

        // Trim inputs and collect MR numbers
        Set<String> mrNumbers = new HashSet<>();
        for (PackingListCreationIUMTEntity e : toCreate) {
            if (e.getMrNumber() != null) {
                e.setMrNumber(e.getMrNumber().trim());
                mrNumbers.add(e.getMrNumber());
            }
            if (e.getLineNumber() != null) {
                e.setLineNumber(e.getLineNumber().trim());
            }
        }

        Map<String, MaterialRequestSummaryHeader> headerByMr = new HashMap<>();
        if (!mrNumbers.isEmpty()) {
            List<MaterialRequestSummaryHeader> headers =
                    materialRequestSummaryHeaderRepository.findByMrNumberInWithItems(new ArrayList<>(mrNumbers));
            for (MaterialRequestSummaryHeader h : headers) {
                headerByMr.put(h.getMrNumber(), h);
            }
        }

        ZonedDateTime now = ZonedDateTime.now(KOLKATA);
        String ts = now.format(PL_NO_FMT);
        String packingListNo = "PLNO" + ts;

        long existingCount = packingListCreationIUMTRepository.count();
        long counter = existingCount + 1;

        for (PackingListCreationIUMTEntity entity : toCreate) {
            entity.setSlNo(counter++);

            entity.setPackingListNo(packingListNo);

            MaterialRequestSummaryHeader header = headerByMr.get(entity.getMrNumber());
            if (header != null) {
                entity.setUnit(header.getUnitCode());
                entity.setUnitName(header.getUnitName());
                entity.setBillingAddress(header.getDeliveryAddress());

                if (header.getItems() != null && entity.getLineNumber() != null) {
                    header.getItems().stream()
                            .filter(item -> item.getLineNumber() != null && item.getLineNumber().equals(entity.getLineNumber()))
                            .findFirst().ifPresent(matched -> entity.setMaterialType(matched.getMaterialType()));
                }
            }
        }

        return packingListCreationIUMTRepository.saveAll(toCreate);
    }

    @Override
    public List<PackingListCreationIUMTEntity> getAllPackingListsIUMT() {
        return packingListCreationIUMTRepository.findAll();
    }

    @Override
    public List<Map<String, String>> getPackingListNo() {

        List<String> allPackingListNos = packingListCreationIUMTRepository.findPackingListNo();

        List<String> usedPackingListNos = deliveryChallanRepository.findAllPackingListNos();

        List<String> availablePackingListNos = allPackingListNos.stream()
                .filter(pl -> !usedPackingListNos.contains(pl))
                .toList();

        return availablePackingListNos.stream()
                .map(pl -> Map.of("label", pl, "value", pl))
                .collect(Collectors.toList());
    }

    @Override
    public List<PackingListCreationIUMTEntity> getByPackingListNo(String packingListNo) {
        if (packingListNo == null || packingListNo.isBlank()) {
            return List.of();
        }
        return packingListCreationIUMTRepository.findAllByPackingListNo(packingListNo.trim());
    }

    @Override
    public void deleteAllPackingLists() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║    🗑️  DELETE ALL PACKING LISTS       ║");
        System.out.println("╚════════════════════════════════════════╝\n");

        try {
            long totalCount = packingListCreationIUMTRepository.count();
            System.out.println("📊 Total packing lists before deletion: " + totalCount);

            packingListCreationIUMTRepository.deleteAll();

            long afterCount = packingListCreationIUMTRepository.count();
            System.out.println("✅ All packing lists deleted successfully!");
            System.out.println("📊 Total packing lists after deletion: " + afterCount);
            System.out.println("\n╔════════════════════════════════════════╗");
            System.out.println("║     ✅ DELETION COMPLETE               ║");
            System.out.println("╚════════════════════════════════════════╝\n");
        } catch (Exception e) {
            System.err.println("❌ Error deleting all packing lists: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to delete all packing lists: " + e.getMessage());
        }
    }
}
