package com.indona.invento.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.indona.invento.entities.StockoutEntity;
  
@Repository
public interface StockoutRepository extends JpaRepository<StockoutEntity, Long> {

	List<StockoutEntity> findByStoreIdAndDeleteFlag(Long id, int i);

	List<StockoutEntity> findByDeleteFlag(int i);

	@Query("SELECT s FROM StockoutEntity s WHERE s.deleteFlag = :deleteFlag AND " +
	           "(CAST(s.id AS string) LIKE %:search% OR s.customerName LIKE %:search% OR s.transferStage LIKE %:search% OR s.paymentStatus LIKE %:search%)")
	List<StockoutEntity> findByDeleteFlagOrderByDateTimeDesc(int deleteFlag, String search, Pageable pageable);

	@Query("SELECT s FROM StockoutEntity s WHERE s.storeId = :storeId AND s.deleteFlag = :deleteFlag AND " +
	           "(CAST(s.id AS string) LIKE %:search% OR s.customerName LIKE %:search% OR s.transferStage LIKE %:search% OR s.paymentStatus LIKE %:search%)")
	List<StockoutEntity> findByStoreIdAndDeleteFlagOrderByDateTimeDesc(Long storeId, int deleteFlag, String search, Pageable pageable);

	@Query("SELECT COUNT(*) FROM StockoutEntity WHERE deleteFlag = 0 AND storeId = ?3 AND (transferStage = 'completed' OR transferStage = 'returned' OR transferStage = 'discountApproved') AND (paymentStatus = 'received' OR paymentStatus = 'creditInvoice') AND (CAST(totalBill AS FLOAT) > 0) AND dateTime BETWEEN ?1 AND ?2")
	Long getTotalInvoice(Date fromDate, Date toDate, Long storeId);

	@Query("SELECT SUM(CAST(totalBill AS FLOAT) + (CAST(taxPercentage AS DOUBLE)/100 * CAST(totalBill AS FLOAT)) + CAST(labourCharges AS FLOAT) - CAST(discountAmount AS FLOAT)) FROM StockoutEntity WHERE deleteFlag = 0 AND storeId = ?3 AND (transferStage = 'completed' OR transferStage = 'returned' OR transferStage = 'discountApproved') AND (paymentStatus = 'received' AND paymentType = 'cash') AND dateTime BETWEEN ?1 AND ?2")	
	Long getReceivedPayment(Date fromDate, Date toDate, Long storeId);

	@Query("SELECT SUM(CAST(totalBill AS FLOAT)) FROM StockoutEntity WHERE deleteFlag = 0 AND storeId = ?3 AND (transferStage = 'completed' OR transferStage = 'returned' OR transferStage = 'discountApproved') AND dateTime BETWEEN ?1 AND ?2")
	Long getReturnedPayment(Date fromDate, Date toDate, Long storeId);

	@Query("SELECT id FROM StockoutEntity WHERE deleteFlag = 0 AND storeId = ?3 AND (transferStage = 'completed' OR transferStage = 'returned' OR transferStage = 'discountApproved')  AND dateTime BETWEEN ?1 AND ?2")
	List<String> getTransferNumbers(Date fromDate, Date toDate, Long storeId);

	
	
	@Query("SELECT COUNT(*) FROM StockoutEntity WHERE deleteFlag = 0 AND (transferStage = 'completed' OR transferStage = 'returned' OR transferStage = 'discountApproved') AND (paymentStatus = 'received' OR paymentStatus = 'creditInvoice') AND dateTime BETWEEN ?1 AND ?2")
	Long getTotalInvoiceOverall(Date fromDate, Date toDate);

	@Query("SELECT SUM(CAST(totalBill AS FLOAT)+ (CAST(taxPercentage AS DOUBLE)/100 * CAST(totalBill AS FLOAT)) + CAST(labourCharges AS FLOAT) - CAST(discountAmount AS FLOAT)) FROM StockoutEntity WHERE deleteFlag = 0 AND (transferStage = 'completed' OR transferStage = 'returned' OR transferStage = 'discountApproved') AND (paymentStatus = 'received' AND paymentType = 'cash') AND dateTime BETWEEN ?1 AND ?2")
	Long getReceivedPaymentOverall(Date fromDate, Date toDate);

	@Query("SELECT SUM(CAST(totalBill AS FLOAT)) FROM StockoutEntity WHERE deleteFlag = 0 AND (transferStage = 'completed' OR transferStage = 'returned' OR transferStage = 'discountApproved') AND dateTime BETWEEN ?1 AND ?2")
	Long getReturnedPaymentOverall(Date fromDate, Date toDate);

	@Query("SELECT id FROM StockoutEntity WHERE deleteFlag = 0 AND transactionType != 'service' AND (transferStage = 'completed' OR transferStage = 'returned' OR transferStage = 'discountApproved') AND dateTime BETWEEN ?1 AND ?2")
	List<Long> getTransferNumbersOverall(Date fromDate, Date toDate);

	@Query("SELECT s FROM StockoutEntity s WHERE s.deleteFlag = :deleteFlag AND " +
	           "(s.customerPhone LIKE %:search%)")
	List<StockoutEntity> findByPhoneDeleteFlagOrderByDateTimeDesc(int deleteFlag, String search, Pageable pageable);

	@Query("SELECT s FROM StockoutEntity s WHERE s.storeId = :storeId AND s.deleteFlag = :deleteFlag AND " +
	           "(s.customerPhone LIKE %:search%)")
	List<StockoutEntity> findByPhoneStoreIdAndDeleteFlagOrderByDateTimeDesc(Long storeId, int deleteFlag, String search,
			Pageable pageable);

	@Query("SELECT s FROM StockoutEntity s WHERE (s.storeId = :storeId OR s.storeId = :warehouseId) AND s.deleteFlag = :deleteFlag AND " +
	           "(s.customerPhone LIKE %:search%)")
	List<StockoutEntity> findByPhoneStoreIdAndWarehouseIdDeleteFlagOrderByDateTimeDesc(Long warehouseId, Long storeId, int deleteFlag,
			String search, Pageable pageable);

	@Query("SELECT s FROM StockoutEntity s WHERE (s.storeId = :storeId OR s.storeId = :warehouseId) AND s.deleteFlag = :deleteFlag AND " +
	           "(CAST(s.id AS string) LIKE %:search% OR s.customerName LIKE %:search% OR s.transferStage LIKE %:search% OR s.paymentStatus LIKE %:search%)")
	List<StockoutEntity> findByStoreIdAndWarehouseIdDeleteFlagOrderByDateTimeDesc(Long warehouseId, Long storeId, int deleteFlag,
			String search, Pageable pageable);

	@Query("SELECT s FROM StockoutEntity s WHERE (s.deleteFlag = :deleteFlag AND s.transferStage = :status) AND" +
	           "(CAST(s.id AS string) LIKE %:search% OR s.customerName LIKE %:search% OR s.transferStage LIKE %:search% OR s.paymentStatus LIKE %:search%)")
	List<StockoutEntity> findByDeleteFlagAndTransferStageOrderByDateTimeDesc(int deleteFlag, String status, String search, Pageable pageable);

	@Query("SELECT s FROM StockoutEntity s WHERE (s.storeId = :storeId AND s.deleteFlag = :deleteFlag AND s.transferStage = :status) AND" +
	           "(CAST(s.id AS string) LIKE %:search% OR s.customerName LIKE %:search% OR s.transferStage LIKE %:search% OR s.paymentStatus LIKE %:search%)")
	List<StockoutEntity> findByDeleteFlagAndTransferStageAndStoreOrderByDateTimeDesc(int deleteFlag, String status, Long storeId,
			String search, Pageable pageable);

	@Query("SELECT s FROM StockoutEntity s WHERE (s.storeId = :storeId AND s.deleteFlag = 0 AND (s.transferStage = 'completed' OR s.transferStage = 'discountApproved' OR s.transferStage = 'returned'))")
	List<StockoutEntity> getAllInvoices(Long storeId);
}