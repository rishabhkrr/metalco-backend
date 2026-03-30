package com.indona.invento.services;

import com.indona.invento.entities.ManualCashInHandEntity;
import java.util.Date;
import java.util.List;

public interface ManualCashInHandService {
	ManualCashInHandEntity saveManualCashInHand(ManualCashInHandEntity manualCashInHand);

	List<ManualCashInHandEntity> getManualCashInHandByDateRange(Date fromDate, Date toDate, Long storeId);
}
