package com.indona.invento.services.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.indona.invento.dao.DealerRepository;
import com.indona.invento.dto.ExcelRow;
import com.indona.invento.entities.BinsEntity;
import com.indona.invento.entities.DealerEntity;
import com.indona.invento.entities.SkuEntity;
import com.indona.invento.entities.StockinEntity;
import com.indona.invento.services.DealerService;

import org.springframework.stereotype.Service;

@Service
public class DealerServiceImpl implements DealerService {

	@Autowired
    private DealerRepository dealerRepo;
	
	@Override
	public List<DealerEntity> getAllDealers() {
		return dealerRepo.findAll();
	}

	@Override
	public DealerEntity getDealerById(Long id) {
		return dealerRepo.findById(id).get();
	}

	@Override
	public DealerEntity createDealer(DealerEntity supplier) {
		return dealerRepo.save(supplier);
	}

	@Override
	public DealerEntity updateDealer(Long id, DealerEntity supplier) {
		if (dealerRepo.existsById(id)) {
			supplier.setId(id);
            return dealerRepo.save(supplier);
        }
        return null; // Or throw an exception indicating customer not found
	}

	@Override
	public void deleteDealer(Long id) {
		dealerRepo.deleteById(id);

	}
	
	 @Override
//   @Transactional
   public void processExcelData(List<ExcelRow> rows) {
   	Long count = 0L;
   	Long rowsSize = Long.valueOf(rows.size());
       for (ExcelRow row : rows) {
    	   DealerEntity dealer = dealerRepo.findByDealerName(row.getSkuCode());
           if (dealer == null) {
        	   dealer = new DealerEntity();
        	   dealer.setDealerName(row.getSkuCode());
        	   dealer.setPhone(row.getSkuName());
        	   dealer.setAppointedBy(row.getServicePrice());
        	   
        	   String dateString = row.getRetailPrice(); // Assuming this returns "3/1/2023"
        	   SimpleDateFormat dateFormat = new SimpleDateFormat("M/d/yyyy");
        	   try {
        		   if(dateString != null) {
        			   Date appointmentDate = dateFormat.parse(dateString); 
        			   dealer.setAppointmentDate(appointmentDate);
        		   }
        		   
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	   dealer.setStatus(1);
        	   dealer = dealerRepo.save(dealer);
			} else {
			   dealer.setPhone(row.getSkuName());
			   dealer = dealerRepo.save(dealer);
           }
		   count++;
		   System.out.println("Saved:"+count+"/"+rowsSize);
       }
   }

}
