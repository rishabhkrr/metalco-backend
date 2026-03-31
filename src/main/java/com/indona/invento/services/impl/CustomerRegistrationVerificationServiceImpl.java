package com.indona.invento.services.impl;

import com.indona.invento.dao.CustomerRegistrationVerificationRepository;
import com.indona.invento.dto.CustomerRegistrationVerificationDto;
import com.indona.invento.entities.CustomerRegistrationVerificationEntity;
import com.indona.invento.services.CustomerRegistrationVerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
@Service
public class CustomerRegistrationVerificationServiceImpl implements CustomerRegistrationVerificationService {

    @Autowired
    private CustomerRegistrationVerificationRepository repository;

    // ✅ Create
    @Override
    public CustomerRegistrationVerificationEntity registerCustomer(CustomerRegistrationVerificationDto dto) {
        if (repository.existsByCustomerEmail(dto.getCustomerEmail())) {
            throw new RuntimeException("Customer already exists with this email");
        }

        CustomerRegistrationVerificationEntity customer = mapDtoToEntity(dto);
        customer.setCustomerCode("CUST" + System.currentTimeMillis()); // Auto-generate
        customer.setPan(dto.getPan()); // PAN from DTO

        return repository.save(customer);
    }

    // ✅ Get All
    @Override
    public List<CustomerRegistrationVerificationEntity> getAllCustomers() {
        return repository.findAll();
    }

    // ✅ Get By ID
    @Override
    public CustomerRegistrationVerificationEntity getCustomerById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + id));
    }

    // ✅ Update
    @Override
    public CustomerRegistrationVerificationEntity updateCustomer(Long id, CustomerRegistrationVerificationDto dto) {
        CustomerRegistrationVerificationEntity existing = getCustomerById(id);

        CustomerRegistrationVerificationEntity updated = mapDtoToEntity(dto);
        updated.setId(id);
        updated.setCustomerCode(existing.getCustomerCode()); // preserve code
        updated.setPan(dto.getPan()); // PAN from DTO

        return repository.save(updated);
    }

    // ✅ Delete
    @Override
    public void deleteCustomer(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Customer not found with ID: " + id);
        }
        repository.deleteById(id);
    }

    // ✅ DTO → Entity Mapper
    private CustomerRegistrationVerificationEntity mapDtoToEntity(CustomerRegistrationVerificationDto dto) {
        CustomerRegistrationVerificationEntity customer = new CustomerRegistrationVerificationEntity();
        customer.setCustomerName(dto.getCustomerName());
        customer.setCustomerAddress(dto.getCustomerAddress());
        customer.setCustomerPhoneNo(dto.getCustomerPhoneNo());
        customer.setCustomerEmail(dto.getCustomerEmail());
        customer.setCustomerArea(dto.getCustomerArea());
        customer.setState(dto.getState());
        customer.setCountry(dto.getCountry());
        customer.setPincode(dto.getPincode());
        customer.setGstUin(dto.getGstUin());
        customer.setAltEmail(dto.getAltEmail());
        customer.setAltPhone(dto.getAltPhone());
        customer.setGstCertificate(dto.getGstCertificate());
        customer.setRemarks(dto.getRemarks());
        customer.setUnit(dto.getUnit());
        customer.setCreditDays(dto.getCreditDays());
        customer.setCreditLimit(dto.getCreditLimit());
        customer.setCustomerCategory(dto.getCustomerCategory());
        customer.setAdditionalRemarks(dto.getAdditionalRemarks());
        return customer;
    }
}
