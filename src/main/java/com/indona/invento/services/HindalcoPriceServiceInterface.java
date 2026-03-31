package com.indona.invento.services;

import com.indona.invento.dto.HindalcoPriceDto;
import com.indona.invento.entities.HindalcoPriceEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Date;
import java.util.List;

public interface HindalcoPriceServiceInterface {

    List<HindalcoPriceEntity> generateToday(); // now handles today + missing dates

    Page<HindalcoPriceEntity> getAllPrices(Pageable pageable);
    
    List<HindalcoPriceEntity> getAllPricesWithoutPagination();

    HindalcoPriceEntity getByDate(Date date);

    HindalcoPriceEntity updatePrice(Long id, HindalcoPriceDto dto);

    HindalcoPriceEntity getLatestPrice();

}
