package com.indona.invento.services;

import com.indona.invento.dto.CustomerMasterDto;
import com.indona.invento.entities.CustomerMasterEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CustomerMasterService {
    CustomerMasterEntity createCustomer(CustomerMasterDto dto);
    CustomerMasterEntity updateCustomer(Long id, CustomerMasterDto dto);
    boolean deleteCustomer(Long id);
    Page<CustomerMasterEntity> getAllCustomers(Pageable pageable);
    CustomerMasterEntity getCustomerById(Long id);
    boolean isCustomerNameExists(String customerName);
    List<CustomerMasterEntity> getAllCustomersWithoutPagination();
    CustomerMasterEntity approveCustomer(Long id) throws Exception;
    CustomerMasterEntity rejectCustomer(Long id) throws Exception;
}
