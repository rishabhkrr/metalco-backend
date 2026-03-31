package com.indona.invento.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.indona.invento.entities.StockReturnEntity;
  
@Repository
public interface StockReturnRepository extends JpaRepository<StockReturnEntity, Long> {

	List<StockReturnEntity> findByStoreIdAndDeleteFlag(Long id, int i);

	List<StockReturnEntity> findByDeleteFlag(int i);

	@Query("SELECT s FROM StockReturnEntity s WHERE s.deleteFlag = :deleteFlag AND " +
	           "(CAST(s.id AS string) LIKE %:search% OR s.transferStage LIKE %:search%)")
	List<StockReturnEntity> findByDeleteFlagOrderByDateTimeDesc(int deleteFlag, String search, Pageable pageable);

	@Query("SELECT s FROM StockReturnEntity s WHERE s.storeId = :storeId AND s.deleteFlag = :deleteFlag AND " +
	           "(CAST(s.id AS string) LIKE %:search% OR s.transferStage LIKE %:search% )")
	List<StockReturnEntity> findByStoreIdAndDeleteFlagOrderByDateTimeDesc(Long storeId, int deleteFlag, String search, Pageable pageable);

	@Query("SELECT s FROM StockReturnEntity s WHERE s.deleteFlag = :deleteFlag AND " +
	           "(CAST(s.seqNo AS string) LIKE %:search%)")
	List<StockReturnEntity> findByPhoneDeleteFlagOrderByDateTimeDesc(int deleteFlag, String search, Pageable pageable);

	@Query("SELECT s FROM StockReturnEntity s WHERE s.storeId = :storeId AND s.deleteFlag = :deleteFlag AND " +
	           "(CAST(s.seqNo AS string) LIKE %:search%)")
	List<StockReturnEntity> findByPhoneStoreIdAndDeleteFlagOrderByDateTimeDesc(Long storeId, int deleteFlag, String search,
			Pageable pageable);

	@Query("SELECT s FROM StockReturnEntity s WHERE (s.storeId = :storeId OR s.storeId = :warehouseId) AND s.deleteFlag = :deleteFlag AND " +
	           "(CAST(s.seqNo AS string) LIKE %:search%)")
	List<StockReturnEntity> findByPhoneStoreIdAndWarehouseIdDeleteFlagOrderByDateTimeDesc(Long warehouseId, Long storeId, int deleteFlag,
			String search, Pageable pageable);

	@Query("SELECT s FROM StockReturnEntity s WHERE (s.storeId = :storeId OR s.storeId = :warehouseId) AND s.deleteFlag = :deleteFlag AND " +
	           "(CAST(s.id AS string) LIKE %:search% OR s.transferStage LIKE %:search% )")
	List<StockReturnEntity> findByStoreIdAndWarehouseIdDeleteFlagOrderByDateTimeDesc(Long warehouseId, Long storeId, int deleteFlag,
			String search, Pageable pageable);

	@Query("SELECT s FROM StockReturnEntity s WHERE (s.deleteFlag = :deleteFlag AND s.transferStage = :status) AND" +
	           "(CAST(s.id AS string) LIKE %:search% OR s.transferStage LIKE %:search% )")
	List<StockReturnEntity> findByDeleteFlagAndTransferStageOrderByDateTimeDesc(int deleteFlag, String status, String search, Pageable pageable);

	@Query("SELECT s FROM StockReturnEntity s WHERE (s.storeId = :storeId AND s.deleteFlag = :deleteFlag AND s.transferStage = :status) AND" +
	           "(CAST(s.id AS string) LIKE %:search% OR s.transferStage LIKE %:search% )")
	List<StockReturnEntity> findByDeleteFlagAndTransferStageAndStoreOrderByDateTimeDesc(int deleteFlag, String status, Long storeId,
			String search, Pageable pageable);

	@Query("SELECT s FROM StockReturnEntity s WHERE (s.storeId = :storeId AND s.deleteFlag = 0 AND (s.transferStage = 'completed' OR s.transferStage = 'discountApproved'))")
	List<StockReturnEntity> getAllInvoices(Long storeId);
	
	@Query("SELECT CAST(seqNo AS string) FROM StockReturnEntity WHERE deleteFlag = 0 AND returnType = 'invoice'  AND storeId = ?3 AND dateTime BETWEEN ?1 AND ?2")
	List<String> getTransferNumbers(Date fromDate, Date toDate, Long storeId);
	
	@Query("SELECT CAST(seqNo AS string) FROM StockReturnEntity WHERE deleteFlag = 0 AND returnType = 'invoice' AND dateTime BETWEEN ?1 AND ?2")
	List<String> getTransferNumbersOverall(Date fromDate, Date toDate);
}