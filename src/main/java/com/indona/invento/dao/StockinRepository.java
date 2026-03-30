package com.indona.invento.dao;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.indona.invento.entities.StockinEntity;

import jakarta.transaction.Transactional;

@Repository
public interface StockinRepository extends JpaRepository<StockinEntity, Long> {

	List<StockinEntity> findBySkuIdAndStoreIdOrderByDateTimeAsc(Long skuId, Long storeId);

	List<StockinEntity> findByStoreIdAndDeleteFlag(Long storeId, int i);

	List<StockinEntity> findByDeleteFlag(int i);

	List<StockinEntity> findBySkuIdAndStoreIdAndBinIdOrderByDateTimeAsc(Long skuId, Long storeId, Long binId);

	List<StockinEntity> findBySkuIdAndStoreIdAndDeleteFlagOrderByDateTimeAsc(Long skuId, Long storeId, int i);

	List<StockinEntity> findByStoreIdAndDeleteFlagOrderByDateTimeDesc(Long storeId, int i, Pageable pageable);

	@Query("SELECT s FROM StockinEntity s JOIN SkuEntity sku ON s.skuId = sku.id WHERE s.deleteFlag = :deleteFlag AND "
			+ "(CAST(s.id AS string) LIKE %:search% OR s.skuName LIKE %:search% OR s.transferNumber LIKE %:search% OR sku.skuCode LIKE %:search% OR s.type LIKE %:search%) "
			+ "ORDER BY s.dateTime DESC")
	List<StockinEntity> findByDeleteFlagOrderByDateTimeDesc(@Param("deleteFlag") int deleteFlag,
			@Param("search") String search, Pageable pageable);

	@Query("SELECT s FROM StockinEntity s JOIN SkuEntity sku ON s.skuId = sku.id "
			+ "WHERE s.storeId = :storeId AND s.deleteFlag = :deleteFlag AND "
			+ "(CAST(s.id AS string) LIKE %:search% OR s.skuName LIKE %:search% OR s.transferNumber LIKE %:search% OR sku.skuCode LIKE %:search% OR s.type LIKE %:search%)")
	List<StockinEntity> searchByStoreIdAndFields(@Param("storeId") Long storeId, @Param("deleteFlag") int deleteFlag,
			@Param("search") String search, Pageable pageable);

	@Query("SELECT s.skuId, s.skuName, s.storeId, " + "SUM(s.skuQuantity - s.skuHold) as availableQuantity "
			+ "FROM StockinEntity s " + "WHERE s.deleteFlag = 0 " + "GROUP BY s.skuId, s.skuName, s.storeId")
	List<Object> getAllStockInReport();

	// New query to filter out stock-in records that have matching partNo in
	// New method to update quantity in StockinEntity based on matching bin and part
	// number
	@Modifying
	@Transactional
	@Query("UPDATE StockinEntity s SET s.skuQuantity = s.skuQuantity - :damageQuantity "
			+ "WHERE s.binId = (SELECT bin.id FROM BinsEntity bin WHERE bin.binName = :binName) "
			+ "AND s.skuId = (SELECT sku.id FROM SkuEntity sku WHERE sku.skuCode = :partNumber) "
			+ "AND s.deleteFlag = 0")
	int updateStockQuantityByBinAndPartNumber(@Param("binName") String binName, @Param("partNumber") String partNumber,
			@Param("damageQuantity") Long damageQuantity);

	// DamageEntity
	// Adding this method to fetch stock-in data excluding damaged products
	@Query("SELECT s FROM StockinEntity s WHERE s.deleteFlag = :deleteFlag AND "
			+ "(LOWER(s.skuName) LIKE LOWER(CONCAT('%', :search, '%')))")
	List<StockinEntity> findStockInExcludingDamaged(int deleteFlag, String search, Pageable pageable);
}
