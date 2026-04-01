package com.indona.invento.services;

import com.indona.invento.dto.PackingListTransferDTO;
import com.indona.invento.dto.RfdListSummaryDTO;
import com.indona.invento.dto.RfdListItemDTO;
import com.indona.invento.dto.RfdListBatchDTO;
import com.indona.invento.entities.PackingListTransferEntity;

import java.util.List;

public interface PackingListTransferService {
    List<PackingListTransferEntity> savePackingList(List<PackingListTransferDTO> dtos);
    List<PackingListTransferEntity> getAllPackingLists();

    // New RFD List Summary APIs
    List<RfdListSummaryDTO> getRfdListSummary();
    List<RfdListItemDTO> getRfdListItems(String packingListNumber);
    List<RfdListBatchDTO> getRfdListBatchDetails(String packingListNumber, Long itemId);
}
