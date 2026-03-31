package com.indona.invento.services;

import com.indona.invento.dto.PurchaseFollowUpUpdateDTO;

public interface PurchaseFollowUpService {
    boolean markFollowUpCompleted(PurchaseFollowUpUpdateDTO dto);
    void deleteAllFollowUps();
}
