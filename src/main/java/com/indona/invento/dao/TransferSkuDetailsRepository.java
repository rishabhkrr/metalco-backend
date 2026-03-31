package com.indona.invento.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.indona.invento.entities.BinsEntity;
import com.indona.invento.entities.PicklistEntity;
import com.indona.invento.entities.StockTransferSkuEntity;
  
@Repository
public interface TransferSkuDetailsRepository extends JpaRepository<StockTransferSkuEntity, Long> { 

	List<StockTransferSkuEntity> findAllByTransferNumber(String trnNumber);
	
	StockTransferSkuEntity findByTransferNumberAndTransferSkuCode(String trnNumber, String skuCode);

	boolean existsByTransferNumber(String valueOf);
	
	void deleteByTransferNumber(String valueOf);

	void deleteByTransferNumberAndTransferSkuCode(String transferNumber, String transferSkuCode);

	@Query("SELECT SUM((CAST(s.returnedQuantity AS FLOAT)) * CAST(s.skuPrice AS FLOAT)) " +
		       "FROM StockTransferSkuEntity s " +
		       "WHERE s.skuPrice IS NOT NULL AND s.transferNumber IN :transferNumbers AND s.dateTime BETWEEN :fromDate AND :toDate")
	Long getReturnedPayment(@Param("fromDate") Date fromDate, 
	                        @Param("toDate") Date toDate, 
	                        @Param("transferNumbers") List<String> transferNumbers);

	
	@Query("SELECT SUM((CAST(s.returnedQuantity AS FLOAT)) * CAST(s.skuPrice AS FLOAT)) " +
		       "FROM StockTransferSkuEntity s " +
		       "WHERE s.skuPrice IS NOT NULL AND s.transferNumber IN :transferNumbers AND s.dateTime BETWEEN :fromDate AND :toDate")
	Long getReturnedPaymentOverall(@Param("fromDate") Date fromDate, 
	                        @Param("toDate") Date toDate, 
	                        @Param("transferNumbers") List<String> transferNumbers);


}