package com.indona.invento.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.indona.invento.entities.DealerEntity;
import com.indona.invento.entities.LedgerEntity;
import com.indona.invento.entities.SupplierEntity;
  
@Repository
public interface LedgerRepository extends JpaRepository<LedgerEntity, Long> {

	List<LedgerEntity> findByType(String type);
	
	@Query("SELECT s FROM LedgerEntity s WHERE s.type = ?1 AND s.dealerId = ?2 AND s.dateTime BETWEEN ?3 AND ?4")
	List<LedgerEntity> findByTypeAndDealerIdAndBetweenDateTime(String type, Long id, Date start, Date end);

	List<LedgerEntity> findByStoreAndType(Long store, String type);

	List<LedgerEntity> findByStore(Long store);

	@Query("SELECT SUM(CAST(credit AS FLOAT)) FROM LedgerEntity WHERE store = ?3 AND type = ?4 AND dateTime BETWEEN ?1 AND ?2")
	Long getTotalExpenses(Date fromDate, Date toDate, Long storeId, String type);

	@Query("SELECT SUM(CAST(debit AS FLOAT)) FROM LedgerEntity WHERE store = ?3 AND (type = 'dealer' OR type = 'pgm') AND dateTime BETWEEN ?1 AND ?2")
	Long getTotalDebit(Date fromDate, Date toDate, Long storeId);

	@Query("SELECT SUM(CAST(credit AS FLOAT)) FROM LedgerEntity WHERE store = ?3 AND (type = 'dealer' OR type = 'pgm') AND  dateTime BETWEEN ?1 AND ?2")
	Long getTotalCredit(Date fromDate, Date toDate, Long storeId);


	@Query("SELECT SUM(CAST(credit AS FLOAT)) FROM LedgerEntity WHERE type = ?3 AND dateTime BETWEEN ?1 AND ?2")
	Long getTotalExpensesOverall(Date fromDate, Date toDate, String type);

	@Query("SELECT SUM(CAST(debit AS FLOAT)) FROM LedgerEntity WHERE (type = 'dealer' OR type = 'pgm') AND dateTime BETWEEN ?1 AND ?2")
	Long getTotalDebitOverall(Date fromDate, Date toDate);

	@Query("SELECT SUM(CAST(credit AS FLOAT)) FROM LedgerEntity WHERE (type = 'dealer' OR type = 'pgm') AND  dateTime BETWEEN ?1 AND ?2")
	Long getTotalCreditOverall(Date fromDate, Date toDate);

}