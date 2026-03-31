package com.indona.invento.services;

import com.indona.invento.dto.AvailableStockDto;
import com.indona.invento.dto.ItemEnquiryDTO;
import com.indona.invento.entities.BillingSummaryEntity;
import com.indona.invento.entities.ItemEnquiry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ItemEnquiryService {
    ItemEnquiry create(ItemEnquiryDTO dto);
    Page<ItemEnquiry> getAll(Pageable pageable);
    List<ItemEnquiry> getAllWithoutPagination();
    Optional<ItemEnquiry> getById(Long id);
    void delete(Long id);
    ItemEnquiry update(Long id, ItemEnquiryDTO dto);
    AvailableStockDto getAvailableStockList(String productCategory, String itemDescription, String brand,
                                            String grade, String temper, String materialType,
                                            String dimension, BigDecimal requiredQuantity, String uom, String unit, String orderType);

    ItemEnquiry cancelAndDeleteEnquiry(Long id);

    ItemEnquiry markStatusAsReceived(String quotationNo);

    Map<String, Long> getStatusSummary();

    Map<String, String> getItemInfoByDescription(String itemDescription);

    ItemEnquiry updateRemarks(String quotationNo, String remarks);

    Page<ItemEnquiry> getEnquiriesBetweenDates(LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable);
    
    List<ItemEnquiry> getEnquiriesBetweenDatesWithoutPagination(LocalDateTime fromDate, LocalDateTime toDate);



        void deleteAllEnquiries();

    Map<String, Object> getPriceDetail(String itemDescription, String orderType);

    Map<String, Object> getAvailableQuantity(String itemDescription, String unit);

    /**
     * Approve an item enquiry - sets status to APPROVED
     */
    ItemEnquiry approveEnquiry(Long id);

    /**
     * Reject an item enquiry - sets status to REJECTED
     */
    ItemEnquiry rejectEnquiry(Long id);
}

