package com.indona.invento.services.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.indona.invento.dao.PgmRepository;
import com.indona.invento.dto.ExcelRow;
import com.indona.invento.entities.DealerEntity;
import com.indona.invento.entities.PgmEntity;
import com.indona.invento.services.PgmService;

import org.springframework.stereotype.Service;

@Service
public class PgmServiceImpl implements PgmService {

	@Autowired
    private PgmRepository pgmRepo;
	
	@Override
	public List<PgmEntity> getAllPgms() {
		return pgmRepo.findAll();
	}

	@Override
	public PgmEntity getPgmById(Long id) {
		return pgmRepo.findById(id).get();
	}

	@Override
	public PgmEntity createPgm(PgmEntity supplier) {
		return pgmRepo.save(supplier);
	}

	@Override
	public PgmEntity updatePgm(Long id, PgmEntity supplier) {
		if (pgmRepo.existsById(id)) {
			supplier.setId(id);
            return pgmRepo.save(supplier);
        }
        return null; // Or throw an exception indicating customer not found
	}

	@Override
	public void deletePgm(Long id) {
		pgmRepo.deleteById(id);

	}
	
	 @Override
//   @Transactional
   public void processExcelData(List<ExcelRow> rows) {
   	Long count = 0L;
   	Long rowsSize = Long.valueOf(rows.size());
       for (ExcelRow row : rows) {
    	   PgmEntity dealer = pgmRepo.findByPgmName(row.getSkuCode());
           if (dealer == null) {
        	   dealer = new PgmEntity();
        	   dealer.setPgmName(row.getSkuCode());
        	   dealer.setPhone(row.getSkuName());
			   dealer.setStatus(1);
			   dealer = pgmRepo.save(dealer);
			} else {
			   dealer.setPhone(row.getSkuName());
			   dealer = pgmRepo.save(dealer);
           }
		   count++;
		   System.out.println("Saved:"+count+"/"+rowsSize);
       }
   }

}
