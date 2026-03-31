package com.indona.invento.services;

import com.indona.invento.dto.BlockedQuantityRequest;
import com.indona.invento.entities.BlockedQuantityEntity;

public interface BlockedQuantityService {
    BlockedQuantityEntity createBlockedQuantity(BlockedQuantityRequest request);
}
