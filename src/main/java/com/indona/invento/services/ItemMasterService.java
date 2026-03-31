package com.indona.invento.services;

import com.indona.invento.dto.ItemMasterDto;
import com.indona.invento.entities.ItemMasterEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ItemMasterService {
    ItemMasterEntity createItem(ItemMasterDto dto);
    ItemMasterEntity getItemById(Long id);
    Page<ItemMasterEntity> getAllItems(Pageable pageable);
    void deleteItem(Long id);
    ItemMasterEntity updateItem(Long id, ItemMasterDto dto);
    List<ItemMasterEntity> getAllItemsWithoutPagination();
    List<ItemMasterEntity> getAllApprovedItems();
    Optional<ItemMasterEntity> getItemByCategoryAndDescription(String category, String description);
    ItemMasterEntity approveItem(Long id) throws Exception;
    ItemMasterEntity rejectItem(Long id) throws Exception;
    List<ItemMasterEntity> getItemsByCategoryAndBrand(String productCategory, String brand);

    /**
     * Get dimension by item description (skuDescription)
     * Returns dimension1, dimension2, dimension3 combined
     */
    Map<String, Object> getDimensionByItemDescription(String itemDescription);

    /**
     * Bulk upload items from CSV file
     * @param file CSV file with item data
     * @return Upload result with success/failure counts
     */
    Map<String, Object> bulkUploadItems(org.springframework.web.multipart.MultipartFile file);
}
