package com.indona.invento.services.impl;

import com.indona.invento.dto.CurrentPriceSummaryDto;
import com.indona.invento.entities.HindalcoPriceEntity;
import com.indona.invento.entities.NalcoPriceEntity;
import com.indona.invento.services.HindalcoPriceServiceInterface;
import com.indona.invento.services.NalcoPriceService;
import com.indona.invento.services.PriceSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PriceSummaryServiceImpl implements PriceSummaryService {

    private final NalcoPriceService nalcoPriceService;
    private final HindalcoPriceServiceInterface hindalcoPriceService;

    @Override
    public CurrentPriceSummaryDto getSummary() {
        NalcoPriceEntity nalco = nalcoPriceService.getLatestPrice();
        HindalcoPriceEntity hindalco = hindalcoPriceService.getLatestPrice();

        return CurrentPriceSummaryDto.builder()
                .lastInvoiceDate(null)
                .lastSoldPrice(null)
                .nalcoPriceOnLastInvoiceDate(null)
                .currentNalcoPrice(nalco != null ? nalco.getNalcoPrice().doubleValue() : null)
                .hindalcoPriceOnLastInvoiceDate(null)
                .currentHindalcoPrice(hindalco != null ? hindalco.getPrice() : null)
                .build();
    }
}
