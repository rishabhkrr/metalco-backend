package com.indona.invento.dao;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.indona.invento.entities.StockTransferEntity;

  
@Repository
public interface StockTransferRepository extends JpaRepository<StockTransferEntity, Long> {

	List<StockTransferEntity> findByToStoreAndDeleteFlag(Long toStore, int i);

	List<StockTransferEntity> findByDeleteFlag(int i);

	@Query("SELECT s FROM StockTransferEntity s WHERE s.deleteFlag = :deleteFlag AND " +
		       "(s.transferNumber LIKE %:search%) " +
		       "ORDER BY s.dateTime DESC")
	List<StockTransferEntity> findByDeleteFlagOrderByDateTimeDesc(int deleteFlag, String search, Pageable pageable);

	@Query("SELECT s FROM StockTransferEntity s WHERE s.deleteFlag = :deleteFlag AND " +
		       "(s.toStore = :storeId AND s.transferNumber LIKE %:search%) " +
		       "ORDER BY s.dateTime DESC")
	List<StockTransferEntity> findByToStoreAndDeleteFlagOrderByDateTimeDesc(@Param("storeId") Long storeId, int deleteFlag, @Param("search") String search, Pageable pageable);

	@Query("SELECT s FROM StockTransferEntity s WHERE s.deleteFlag = :deleteFlag AND " +
		       "(s.fromStore = :storeId AND s.transferNumber LIKE %:search%) " +
		       "ORDER BY s.dateTime DESC")
	List<StockTransferEntity> findByFromStoreAndDeleteFlagOrderByDateTimeDesc(@Param("storeId") Long storeId, int deleteFlag, @Param("search") String search, Pageable pageable);

	@Query("SELECT s FROM StockTransferEntity s WHERE s.deleteFlag = :deleteFlag AND " +
		       "(s.toStore = :to OR s.fromStore = :from) AND (s.transferNumber LIKE %:search%) " +
		       "ORDER BY s.dateTime DESC")
	List<StockTransferEntity> findByFromStoreOrToStoreAndDeleteFlagOrderByDateTimeDesc(@Param("from") Long from, @Param("to") Long to, int deleteFlag, @Param("search") String search, Pageable pageable);

	List<StockTransferEntity> findByDeleteFlagOrderByDateTimeDesc(int i);

	boolean existsByTransferNumber(String transferNumber); 

}