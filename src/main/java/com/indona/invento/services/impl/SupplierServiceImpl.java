package com.indona.invento.services.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.indona.invento.dao.SupplierRepository;
import com.indona.invento.entities.SupplierEntity;
import com.indona.invento.services.SupplierService;

import org.springframework.stereotype.Service;

@Service
public class SupplierServiceImpl implements SupplierService {

	@Autowired
    private SupplierRepository supplierRepository;
	
	@Override
	public List<SupplierEntity> getAllSuppliers() {
		return supplierRepository.findAll();
	}

	@Override
	public SupplierEntity getSupplierById(Long id) {
		return supplierRepository.findById(id).get();
	}

	@Override
	public SupplierEntity createSupplier(SupplierEntity supplier) {
		return supplierRepository.save(supplier);
	}

	@Override
	public SupplierEntity updateSupplier(Long id, SupplierEntity supplier) {
		if (supplierRepository.existsById(id)) {
			supplier.setId(id);
            return supplierRepository.save(supplier);
        }
        return null; // Or throw an exception indicating customer not found
	}

	@Override
	public void deleteSupplier(Long id) {
		supplierRepository.deleteById(id);

	}

}
