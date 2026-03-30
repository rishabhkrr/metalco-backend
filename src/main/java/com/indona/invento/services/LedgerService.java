package com.indona.invento.services;

import java.util.List;

import com.indona.invento.dto.FilterLedgerDto;
import com.indona.invento.dto.LedgerDto;
import com.indona.invento.entities.LedgerEntity;
import com.indona.invento.entities.LedgerEntity;

public interface LedgerService {

	List<LedgerDto> getAllLedgers();
	List<LedgerDto> getByStore(Long store);
	List<LedgerDto> getByStoreAndType(Long store, String type);
	List<LedgerDto> getByType(String type);
	LedgerEntity getLedgerById(Long id);
	LedgerEntity createLedger(LedgerEntity supplier);
	LedgerEntity updateLedger(Long id, LedgerEntity supplier);
    void deleteLedger(Long id);
	List<LedgerDto> getReportExpense(FilterLedgerDto req);
	List<LedgerDto> getReportPgm(FilterLedgerDto req);
	List<LedgerDto> getReportDealer(FilterLedgerDto req);
}
