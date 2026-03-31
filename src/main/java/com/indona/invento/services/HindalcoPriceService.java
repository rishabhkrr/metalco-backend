package com.indona.invento.services;

import com.indona.invento.dao.HindalcoPriceRepository;
import com.indona.invento.dto.HindalcoPriceDto;
import com.indona.invento.entities.HindalcoPriceEntity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Service
public class HindalcoPriceService implements HindalcoPriceServiceInterface {

    @Autowired
    private HindalcoPriceRepository repository;


    @Override
    public List<HindalcoPriceEntity> generateToday() {
        LocalDate today = LocalDate.now();

        // Get last saved entry
        Optional<HindalcoPriceEntity> lastEntryOpt = repository.findTopByOrderByPriceDateDesc();

        // Safely convert last date to LocalDate
        LocalDate lastDate = lastEntryOpt
                .map(e -> e.getPriceDate().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate())
                .orElse(today.minusDays(1)); // If no entry, start from yesterday

        // Get last price and PDF path
        Double lastPrice = lastEntryOpt.map(HindalcoPriceEntity::getPrice).orElse(0.0);
        String lastPdfPath = lastEntryOpt.map(HindalcoPriceEntity::getPricePdfPath).orElse(null);

        List<HindalcoPriceEntity> generated = new ArrayList<>();

        // Loop from next day after last entry to today
        for (LocalDate date = lastDate.plusDays(1); !date.isAfter(today); date = date.plusDays(1)) {
            java.sql.Date sqlDate = java.sql.Date.valueOf(date);

            // Skip if already exists
            if (repository.findByPriceDate(sqlDate).isPresent()) continue;

            HindalcoPriceEntity entity = HindalcoPriceEntity.builder()
                    .priceDate(sqlDate)
                    .price(lastPrice)
                    .uom("Rs/Kg")
                    .pricePdfPath(lastPdfPath) // replicate PDF
                    .build();

            generated.add(repository.save(entity));
        }

        return generated;
    }


    @Override
    public Page<HindalcoPriceEntity> getAllPrices(Pageable pageable) {
        return repository.findAll(pageable);
    }


    @Override
    public HindalcoPriceEntity getByDate(Date date) {
        return repository.findByPriceDate(date).orElse(null);
    }

    @Override
    public HindalcoPriceEntity updatePrice(Long id, HindalcoPriceDto dto) {
        // 1. Fetch entity
        HindalcoPriceEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Price record not found with ID: " + id));

        // 2. Clone original values BEFORE any mutation
        Double originalPrice = Double.valueOf(entity.getPrice()); // deep copy
        String originalPdf = entity.getPricePdfPath();
        Date originalDate = new Date(entity.getPriceDate().getTime()); // deep copy

        // 3. Apply updates
        if (dto.getPrice() != null) {
            entity.setPrice(dto.getPrice().doubleValue());
        }
        if (dto.getUom() != null) {
            entity.setUom(dto.getUom());
        }
        if (dto.getPricePdfPath() != null) {
            entity.setPricePdfPath(dto.getPricePdfPath());
        }

        HindalcoPriceEntity updated = repository.save(entity);

        // 4. Normalize date
        LocalDate localDate = originalDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        java.sql.Date normalizedDate = java.sql.Date.valueOf(localDate);

        // 5. Fetch future entries
        List<HindalcoPriceEntity> futureEntries = repository.findByPriceDateGreaterThan(normalizedDate);

        // 6. Propagate changes
        for (HindalcoPriceEntity future : futureEntries) {
            System.out.println("🔍 Checking future ID: " + future.getId());
            System.out.println("Future Price: " + future.getPrice());
            System.out.println("Updated: " + updated);

            if (updated != null) {
                boolean isModified = false;

                if (updated.getPrice() != null &&
                        Double.compare(future.getPrice(), updated.getPrice()) != 0) {
                    future.setPrice(updated.getPrice());
                    isModified = true;
                }

                if (updated.getPricePdfPath() != null &&
                        (future.getPricePdfPath() == null ||
                                !future.getPricePdfPath().equals(updated.getPricePdfPath()))) {
                    future.setPricePdfPath(updated.getPricePdfPath());
                    isModified = true;
                }

                if (isModified) {
                    HindalcoPriceEntity saved = repository.save(future);
                    System.out.println("💾 Saved updated entity ID: " + saved.getId());
                } else {
                    System.out.println("⚠️ No changes detected, skipping save.");
                }
            } else {
                System.out.println("⛔ Updated object is null, skipping.");
            }
        }


        return updated;
    }


    @Override
    public List<HindalcoPriceEntity> getAllPricesWithoutPagination() {
        return repository.findAll();
    }

    @Override
    public HindalcoPriceEntity getLatestPrice() {
        return repository.findTopByOrderByPriceDateDesc().orElse(null);
    }



}
