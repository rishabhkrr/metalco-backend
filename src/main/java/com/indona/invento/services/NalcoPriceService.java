package com.indona.invento.services;

import com.indona.invento.dto.NalcoPriceUpdateDto;
import com.indona.invento.entities.NalcoPriceEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface NalcoPriceService {

    // Generate today's price entry based on the previous day's price
    List<NalcoPriceEntity> generateTodayPrice(); // ✅ Correct return type

    // Update the price and optional PDF link
    NalcoPriceEntity updateNalcoPrice(Long id, NalcoPriceUpdateDto dto);

    // Get all NALCO price entries
    Page<NalcoPriceEntity> getAllPrices(Pageable pageable);
    
    List<NalcoPriceEntity> getAllPricesWithoutPagination();

    NalcoPriceEntity getLatestPrice();



//    #hell0
}
