package com.indona.invento.dao;


import com.indona.invento.entities.StockSummaryEntity;
import com.indona.invento.services.ItemDetailsProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockSummaryRepository extends JpaRepository<StockSummaryEntity, Long> {
    @Query("SELECT s FROM StockSummaryEntity s WHERE " +
            "(:productCategory IS NULL OR LOWER(TRIM(s.productCategory)) = LOWER(TRIM(:productCategory))) AND " +
            "(:brand IS NULL OR LOWER(TRIM(:brand)) = 'any' OR LOWER(TRIM(s.brand)) = LOWER(TRIM(:brand))) AND " +
            "(:grade IS NULL OR LOWER(TRIM(:grade)) = 'any' OR LOWER(TRIM(s.grade)) = LOWER(TRIM(:grade))) AND " +
            "(:temper IS NULL OR LOWER(TRIM(:temper)) = 'any' OR LOWER(TRIM(s.temper)) = LOWER(TRIM(:temper))) AND " +
            "(:materialType IS NULL OR LOWER(TRIM(:materialType)) = 'any' OR LOWER(TRIM(s.materialType)) = LOWER(TRIM(:materialType))) AND " +
            "(:unit IS NULL OR LOWER(TRIM(:unit)) = 'any' OR LOWER(TRIM(s.unit)) = LOWER(TRIM(:unit))) AND " +
            "(:dimension IS NULL OR LOWER(TRIM(s.dimension)) = LOWER(TRIM(:dimension)) OR LOWER(TRIM(s.dimension)) = LOWER(TRIM(:originalDimension)))")
    List<StockSummaryEntity> findMatchingStock(@Param("productCategory") String productCategory,
                                               @Param("brand") String brand,
                                               @Param("grade") String grade,
                                               @Param("temper") String temper,
                                               @Param("materialType") String materialType,
                                               @Param("dimension") String dimension,
                                               @Param("originalDimension") String originalDimension,  @Param("unit") String unit);


    @Query("SELECT s FROM StockSummaryEntity s " + "WHERE (:unit IS NULL OR s.unit = :unit) " + "AND (:brands IS NULL OR s.brand IN :brands) " + "AND (:productCategories IS NULL OR s.productCategory IN :productCategories) " + "AND (:materialTypes IS NULL OR s.materialType IN :materialTypes)") List<StockSummaryEntity> filterStockSummary( @Param("unit") String unit, @Param("brands") List<String> brands, @Param("productCategories") List<String> productCategories, @Param("materialTypes") List<String> materialTypes );


    Optional<StockSummaryEntity> findTopByItemDescription(String itemDescription);

    @Query("SELECT s.itemPrice FROM StockSummaryEntity s WHERE s.itemDescription = :desc")
    List<BigDecimal> findPricesByItemDescription(@Param("desc") String itemDescription);

    @Query("SELECT s FROM StockSummaryEntity s " +
            "WHERE s.unit = :unit AND s.itemDescription = :itemDescription AND s.store = :store")
    List<StockSummaryEntity> findByUnitAndItemDescriptionAndStore(
            @Param("unit") String unit,
            @Param("itemDescription") String itemDescription,
            @Param("store") String store);

    // Find stock by unit and itemDescription for updating total quantity
    List<StockSummaryEntity> findByUnitAndItemDescription(String unit, String itemDescription);



    @Query("SELECT COALESCE(SUM(s.quantityKg), 0) FROM StockSummaryEntity s WHERE s.itemDescription = :itemDescription AND s.unit = :unit")
    BigDecimal findTotalQuantityKgByItemDescriptionAndUnit(@Param("itemDescription") String itemDescription,
                                                           @Param("unit") String unit);

    @Query("SELECT COALESCE(SUM(s.quantityKg), 0) FROM StockSummaryEntity s " +
           "WHERE (:store IS NULL OR s.store = :store) " +
           "AND (:storageArea IS NULL OR s.storageArea = :storageArea) " +
           "AND (:rackColumnShelfNumber IS NULL OR s.rackColumnShelfNumber = :rackColumnShelfNumber)")
    Double calculateCurrentStorageByLocation(
            @Param("store") String store,
            @Param("storageArea") String storageArea,
            @Param("rackColumnShelfNumber") String rackColumnShelfNumber);

    // Optimized: Fetch all storage quantities grouped by location in single query
    @Query("SELECT s.store, s.storageArea, s.rackColumnShelfNumber, COALESCE(SUM(s.quantityKg), 0) " +
           "FROM StockSummaryEntity s " +
           "GROUP BY s.store, s.storageArea, s.rackColumnShelfNumber")
    List<Object[]> findAllStorageQuantitiesGrouped();

    // Find stock by GRN number in grnNumbers column
    @Query("SELECT s FROM StockSummaryEntity s WHERE s.grnNumbers LIKE %:grnNumber%")
    List<StockSummaryEntity> findByGrnNumberContaining(@Param("grnNumber") String grnNumber);

    // Find by unit, item description, item group for merging
    @Query("SELECT s FROM StockSummaryEntity s WHERE s.unit = :unit AND s.itemDescription = :itemDescription AND s.itemGroup = :itemGroup")
    Optional<StockSummaryEntity> findByUnitAndItemDescriptionAndItemGroup(
            @Param("unit") String unit,
            @Param("itemDescription") String itemDescription,
            @Param("itemGroup") String itemGroup);

    // Find by store, storage area, rack/bin for rack details merge
    @Query("SELECT s FROM StockSummaryEntity s WHERE s.store = :store AND s.storageArea = :storageArea AND s.rackColumnShelfNumber = :rackColumnBin")
    Optional<StockSummaryEntity> findByStoreAndStorageAreaAndRack(
            @Param("store") String store,
            @Param("storageArea") String storageArea,
            @Param("rackColumnBin") String rackColumnBin);

    // Find by unit, item description, item group AND store, storageArea, rack for exact match
    @Query("SELECT s FROM StockSummaryEntity s WHERE s.unit = :unit AND s.itemDescription = :itemDescription AND s.itemGroup = :itemGroup AND s.store = :store AND s.storageArea = :storageArea AND s.rackColumnShelfNumber = :rackColumnBin")
    Optional<StockSummaryEntity> findExactMatch(
            @Param("unit") String unit,
            @Param("itemDescription") String itemDescription,
            @Param("itemGroup") String itemGroup,
            @Param("store") String store,
            @Param("storageArea") String storageArea,
            @Param("rackColumnBin") String rackColumnBin);

    // Find by unit, item description AND store, storageArea, rack (without itemGroup - for Loose Piece etc)
    @Query("SELECT s FROM StockSummaryEntity s WHERE s.unit = :unit AND s.itemDescription = :itemDescription AND s.store = :store AND s.storageArea = :storageArea AND s.rackColumnShelfNumber = :rackColumnBin")
    Optional<StockSummaryEntity> findExactMatchWithoutItemGroup(
            @Param("unit") String unit,
            @Param("itemDescription") String itemDescription,
            @Param("store") String store,
            @Param("storageArea") String storageArea,
            @Param("rackColumnBin") String rackColumnBin);

    // Find by unit, itemDescription, itemGroup, store, storageArea, rack AND dimension (for return stock)
    @Query("SELECT s FROM StockSummaryEntity s WHERE s.unit = :unit AND s.itemDescription = :itemDescription AND s.itemGroup = :itemGroup AND s.store = :store AND s.storageArea = :storageArea AND s.rackColumnShelfNumber = :rackColumnBin AND s.dimension = :dimension")
    Optional<StockSummaryEntity> findByUnitAndItemDescriptionAndItemGroupAndStoreAndStorageAreaAndRackColumnShelfNumberAndDimension(
            @Param("unit") String unit,
            @Param("itemDescription") String itemDescription,
            @Param("itemGroup") String itemGroup,
            @Param("store") String store,
            @Param("storageArea") String storageArea,
            @Param("rackColumnBin") String rackColumnBin,
            @Param("dimension") String dimension);

    @Query("SELECT s FROM StockSummaryEntity s WHERE s.dimension = :normalizedDimension")
    List<StockSummaryEntity> findByDimension(@Param("normalizedDimension") String normalizedDimension);
}


