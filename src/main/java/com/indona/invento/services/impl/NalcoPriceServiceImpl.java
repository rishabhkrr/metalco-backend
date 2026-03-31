package com.indona.invento.services.impl;

import com.indona.invento.dao.NalcoPriceRepository;
import com.indona.invento.dto.NalcoPriceUpdateDto;
import com.indona.invento.entities.NalcoPriceEntity;
import com.indona.invento.services.NalcoPriceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class NalcoPriceServiceImpl implements NalcoPriceService {

    @Autowired
    private NalcoPriceRepository repo;

    @Override
    public List<NalcoPriceEntity> generateTodayPrice() {
        LocalDate today = LocalDate.now();

        Optional<NalcoPriceEntity> lastEntryOpt = repo.findTopByOrderByDateDesc();
        LocalDate lastDate = lastEntryOpt.map(NalcoPriceEntity::getDate).orElse(today.minusDays(1));
        BigDecimal lastPrice = lastEntryOpt.map(NalcoPriceEntity::getNalcoPrice).orElse(BigDecimal.ZERO);
        String lastPdf = lastEntryOpt.map(NalcoPriceEntity::getPricePdf).orElse(null);

        List<NalcoPriceEntity> generated = new ArrayList<>();

        for (LocalDate date = lastDate.plusDays(1); !date.isAfter(today); date = date.plusDays(1)) {
            if (repo.findByDate(date).isPresent()) continue;

            NalcoPriceEntity entity = new NalcoPriceEntity();
            entity.setDate(date);
            entity.setNalcoPrice(lastPrice);
            entity.setUom("Rs/Kg");
            entity.setPricePdf(lastPdf);

            generated.add(repo.save(entity));
        }

        return generated; // ✅ No casting needed
    }



    @Override
    public NalcoPriceEntity updateNalcoPrice(Long id, NalcoPriceUpdateDto dto) {
        NalcoPriceEntity entity = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Nalco price not found with ID: " + id));

        // Clone original values
        BigDecimal originalPrice = entity.getNalcoPrice();
        String originalPdf = entity.getPricePdf();
        LocalDate originalDate = entity.getDate();

        // Apply updates
        if (dto.getNalcoPrice() != null) entity.setNalcoPrice(dto.getNalcoPrice());
        if (dto.getUom() != null) entity.setUom(dto.getUom());
        if (dto.getPricePdf() != null) entity.setPricePdf(dto.getPricePdf());

        NalcoPriceEntity updated = repo.save(entity);

        // Fetch future entries
        List<NalcoPriceEntity> futureEntries = repo.findByDateGreaterThan(originalDate);

        for (NalcoPriceEntity future : futureEntries) {
            boolean isModified = false;

            if (updated.getNalcoPrice() != null &&
                    future.getNalcoPrice().compareTo(updated.getNalcoPrice()) != 0) {
                future.setNalcoPrice(updated.getNalcoPrice());
                isModified = true;
            }

            if (updated.getPricePdf() != null &&
                    (future.getPricePdf() == null ||
                            !future.getPricePdf().equals(updated.getPricePdf()))) {
                future.setPricePdf(updated.getPricePdf());
                isModified = true;
            }

            if (isModified) {
                repo.save(future);
            }
        }

        return updated;
    }


    @Override
    public Page<NalcoPriceEntity> getAllPrices(Pageable pageable) {
        return repo.findAll(pageable);
    }


    @Override
    public List<NalcoPriceEntity> getAllPricesWithoutPagination() {
        return repo.findAll();
    }

    @Override
    public NalcoPriceEntity getLatestPrice() {
        LocalDate today = LocalDate.now();
        Optional<NalcoPriceEntity> todayOpt = repo.findByDate(today);
        if (todayOpt.isEmpty()) {
            // Auto-generate today's price (copying last available)
            generateTodayPrice();
        }
        return repo.findTopByOrderByDateDesc().orElse(null);
    }



}
