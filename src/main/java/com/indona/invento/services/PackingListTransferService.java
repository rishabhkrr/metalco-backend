package com.indona.invento.services;

import com.indona.invento.dto.PackingListTransferDTO;
import com.indona.invento.entities.PackingListTransferEntity;

import java.util.List;

public interface PackingListTransferService {
    List<PackingListTransferEntity> savePackingList(List<PackingListTransferDTO> dtos);
    List<PackingListTransferEntity> getAllPackingLists();
}
