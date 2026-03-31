package com.indona.invento.services;

import com.indona.invento.entities.GRNJobWorkEntity;
import java.util.List;

public interface GRNJobWorkService {
	List<GRNJobWorkEntity> saveAll(List<GRNJobWorkEntity> entities);

	List<GRNJobWorkEntity> getAll();

	GRNJobWorkEntity updateById(Long id, GRNJobWorkEntity updatedEntity);

	void deleteById(Long id);

	List<String> getPendingInvoiceNumbers();

	List<GRNJobWorkEntity> updateMaterialUnloadingStatus(String invoiceNumber, String materialUnloadingStatus);

	GRNJobWorkEntity approveById(Long id);

	GRNJobWorkEntity rejectById(Long id);
}
