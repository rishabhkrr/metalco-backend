package com.indona.invento.services;

import java.util.List;

import com.indona.invento.entities.SupplierEntity;

public interface SupplierService {

	List<SupplierEntity> getAllSuppliers();
	SupplierEntity getSupplierById(Long id);
	SupplierEntity createSupplier(SupplierEntity supplier);
	SupplierEntity updateSupplier(Long id, SupplierEntity supplier);
    void deleteSupplier(Long id);
}
