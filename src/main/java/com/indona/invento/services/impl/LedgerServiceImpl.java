package com.indona.invento.services.impl;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.indona.invento.dao.DealerRepository;
import com.indona.invento.dao.ExpenseCategoryRepository;
import com.indona.invento.dao.LedgerRepository;
import com.indona.invento.dao.PgmRepository;
import com.indona.invento.dao.StoresRepository;
import com.indona.invento.dao.UserInfoRepository;
import com.indona.invento.dto.FilterLedgerDto;
import com.indona.invento.dto.LedgerDto;
import com.indona.invento.entities.LedgerEntity;
import com.indona.invento.services.LedgerService;

import org.springframework.stereotype.Service;

@Service
public class LedgerServiceImpl implements LedgerService {

	@Autowired
    private LedgerRepository ledgerRepo;
	
	@Autowired
    private StoresRepository storeRepo;
	
	@Autowired
	private DealerRepository dealerRepo;
	
	@Autowired
	private PgmRepository pgmRepo;
	
	@Autowired
	private ExpenseCategoryRepository expenseRepo;
	
	@Override
	public List<LedgerDto> getAllLedgers() {
		return ledgerEntityToDto(ledgerRepo.findAll());
	}
	
	@Override
	public List<LedgerDto> getByStore(Long store) {
		return ledgerEntityToDto(ledgerRepo.findByStore(store));
	}

	@Override
	public List<LedgerDto> getByStoreAndType(Long store, String type) {
		return ledgerEntityToDto(ledgerRepo.findByStoreAndType(store, type));
	}

	@Override
	public List<LedgerDto> getByType(String type) {
		return ledgerEntityToDto(ledgerRepo.findByType(type));
	}

	@Override
	public LedgerEntity getLedgerById(Long id) {
		return ledgerRepo.findById(id).get();
	}

	@Override
	public LedgerEntity createLedger(LedgerEntity supplier) {
		return ledgerRepo.save(supplier);
	}

	@Override
	public LedgerEntity updateLedger(Long id, LedgerEntity supplier) {
		if (ledgerRepo.existsById(id)) {
			supplier.setId(id);
            return ledgerRepo.save(supplier);
        }
        return null; // Or throw an exception indicating customer not found
	}

	@Override
	public void deleteLedger(Long id) {
		ledgerRepo.deleteById(id);

	}
	
	private List<LedgerDto> ledgerEntityToDto(List<LedgerEntity> ledger) {
		List<LedgerDto> ledgerDtoList = new ArrayList<>();
		for (LedgerEntity l : ledger) {
			LedgerDto ledgerDto = new LedgerDto();
			ledgerDto.setId(l.getId());
			ledgerDto.setStore(storeRepo.findById(l.getStore()).get().getStoreName());
			ledgerDto.setType(l.getType());
			ledgerDto.setCredit(l.getCredit());
			ledgerDto.setRefNo(l.getRefNo());
			ledgerDto.setDebit(l.getDebit());
			ledgerDto.setExpenseUser(l.getExpenseUser());
			ledgerDto.setReceiptNo(l.getReceiptNo());
			ledgerDto.setDateTime(l.getDateTime());
			if(l.getType().equals("dealer")) {
				ledgerDto.setDealerName(dealerRepo.findById(l.getDealerId()).get().getDealerName());
			} else if(l.getType().equalsIgnoreCase("pgm")) {
				ledgerDto.setDealerName(pgmRepo.findById(l.getDealerId()).get().getPgmName());
			} else if(l.getType().equalsIgnoreCase("expense")) {
				ledgerDto.setDealerName(expenseRepo.findById(l.getDealerId()).get().getCatName());
			} else {
				ledgerDto.setDealerName("Unidetified");
			}
			ledgerDto.setExpenseName(l.getExpenseName());
			ledgerDto.setUserName(l.getUserName());
			ledgerDto.setFile(l.getFile());
			ledgerDtoList.add(ledgerDto);
			
		}
		return ledgerDtoList;
	}

	@Override
	public List<LedgerDto> getReportExpense(FilterLedgerDto req) {
		// TODO Auto-generated method stub
		List<LedgerEntity> list = ledgerRepo.findByTypeAndDealerIdAndBetweenDateTime("expense", req.getDealerId(), req.getFromDate(), req.getToDate());
		
		List<LedgerDto> listDto = ledgerEntityToDto(list);
		return listDto;
	}

	@Override
	public List<LedgerDto> getReportPgm(FilterLedgerDto req) {
		// TODO Auto-generated method stub
		List<LedgerEntity> list = ledgerRepo.findByTypeAndDealerIdAndBetweenDateTime("pgm", req.getDealerId(), req.getFromDate(), req.getToDate());
		List<LedgerDto> listDto = ledgerEntityToDto(list);

		return listDto;
	}

	@Override
	public List<LedgerDto> getReportDealer(FilterLedgerDto req) {
		// TODO Auto-generated method stub
		List<LedgerEntity> list = ledgerRepo.findByTypeAndDealerIdAndBetweenDateTime("dealer", req.getDealerId(), req.getFromDate(), req.getToDate());
		
		List<LedgerDto> listDto = ledgerEntityToDto(list);
		
		return listDto;
	}

}
