package com.indona.invento.services;

import com.indona.invento.dto.CustomerRegistrationVerificationDto;
import com.indona.invento.entities.CustomerRegistrationVerificationEntity;

import java.util.List;

public interface CustomerRegistrationVerificationService {
    CustomerRegistrationVerificationEntity registerCustomer(CustomerRegistrationVerificationDto dto);
    List<CustomerRegistrationVerificationEntity> getAllCustomers();
    CustomerRegistrationVerificationEntity getCustomerById(Long id);
    CustomerRegistrationVerificationEntity updateCustomer(Long id, CustomerRegistrationVerificationDto dto);
    void deleteCustomer(Long id);}

