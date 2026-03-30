package com.indona.invento.services.impl;
import java.util.ArrayList;

import com.indona.invento.dao.CustomerMasterRepository;
import com.indona.invento.dto.CustomerMasterDto;
import com.indona.invento.entities.*;
import com.indona.invento.services.ApprovalWorkflowService;
import com.indona.invento.services.AuditLogService;
import com.indona.invento.services.CustomerMasterService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerMasterServiceImpl implements CustomerMasterService {

    @Autowired
    private CustomerMasterRepository repository;

    @Autowired
    private ApprovalWorkflowService approvalWorkflowService;

    @Autowired
    private AuditLogService auditLogService;

    @Override
    public CustomerMasterEntity createCustomer(CustomerMasterDto dto) {
        String code = generateCustomerCode();

        // Step 1: Build Customer Entity (without child lists for now)
        CustomerMasterEntity customer = CustomerMasterEntity.builder()
                .customerCode(code)
                .gstRegistrationType(dto.getGstRegistrationType())
                .gstOrUin(dto.getGstOrUin())
                .customerCategory(dto.getCustomerCategory())
                .customerName(dto.getCustomerName())
                .mailingBillingName(dto.getMailingBillingName())
                .customerNickname(dto.getCustomerNickname())
                .multipleAddress(dto.getMultipleAddress())
                .creditLimitAmount(dto.getCreditLimitAmount())
                .creditLimitDays(dto.getCreditLimitDays())
                .lockPeriodDays(dto.getLockPeriodDays())
                .pan(dto.getPan())
                .tanNumber(dto.getTanNumber())
                .udyamNumber(dto.getUdyamNumber())
                .iecCode(dto.getIecCode())
                .gstCertificateLink(dto.getGstCertificateLink())
                .otherDocuments(dto.getOtherDocuments())
                .typeOfIndustry(dto.getTypeOfIndustry())
                .applicationOfIndustry(dto.getApplicationOfIndustry())
                .interestCalculation(dto.getInterestCalculation())
                .rateOfInterest(dto.getRateOfInterest())
                .isUdyamAvailable(dto.getIsUdyamAvailable())
                .isTanAvailable(dto.getIsTanAvailable())
                .isIecAvailable(dto.getIsIecAvailable())
                .gstStateCode(dto.getGstStateCode())
                .relatedStatus(dto.getRelatedStatus())
                .panFileUpload(dto.getPanFileUpload())
                .udyamFileUpload(dto.getUdyamFileUpload())
                .typeOfEntity(dto.getTypeOfEntity())
                .status("PENDING_APPROVAL")
                .build();

        // Step 2: Save customer first
        CustomerMasterEntity saved = repository.save(customer);

        // Step 3: Map and link Address Entities
        List<CustomerAddressEntity> addressEntities = dto.getAddressDetails().stream()
                .map(addr -> CustomerAddressEntity.builder()
                        .Primary(addr.getPrimary())
                        .branchName(addr.getBranchName())
                        .address(addr.getAddress())
                        .supplierArea(addr.getSupplierArea())
                        .state(addr.getState())
                        .country(addr.getCountry())
                        .pincode(addr.getPincode())
                        .mapLocation(addr.getMapLocation())
                        .customer(saved)
                        .build())
                .collect(Collectors.toCollection(ArrayList::new));
        saved.setAddressDetails(addressEntities);

        // Step 4: Map and link Contact Entities
        List<CustomerContactEntity> contactEntities = dto.getContactDetails().stream()
                .map(contact -> CustomerContactEntity.builder()
                        .Primary(contact.getPrimary())
                        .name(contact.getName())
                        .designation(contact.getDesignation())
                        .phoneNumber(contact.getPhoneNumber())
                        .emailId(contact.getEmailId())
                        .customer(saved)
                        .build())
                .collect(Collectors.toCollection(ArrayList::new));
        saved.setContactDetails(contactEntities);

        // Step 5: Map and link Bank Details Entities
        List<CustomerBankDetailEntity> bankEntities = dto.getBankDetails().stream()
                .map(bank -> CustomerBankDetailEntity.builder()
                        .beneficiaryName(bank.getBeneficiaryName())
                        .accountNumber(bank.getAccountNumber())
                        .bankName(bank.getBankName())
                        .branchAddress(bank.getBranchAddress())
                        .accountType(bank.getAccountType())
                        .ifscCode(bank.getIfscCode())
                        .micrCode(bank.getMicrCode())
                        .swiftCode(bank.getSwiftCode())
                        .bankCountry(bank.getBankCountry())
                        .upiId(bank.getUpiId())
                        .Primary(bank.getPrimary())
                        .customer(saved)
                        .build())
                .collect(Collectors.toCollection(ArrayList::new));
        saved.setBankDetails(bankEntities);

        // Step 6: Re-save with children linked
        CustomerMasterEntity result = repository.save(saved);

        // Audit + Approval workflow
        auditLogService.logAction("CREATE", "CUSTOMER_MASTER", "CustomerMaster",
                result.getId(), result.getCustomerCode(), null, "PENDING_APPROVAL",
                "Customer " + result.getCustomerName() + " created with code " + result.getCustomerCode(),
                "SYSTEM", null);

        approvalWorkflowService.submitForApproval("CustomerMaster", result.getId(),
                result.getCustomerCode(), "CUSTOMER_MASTER", "SYSTEM", null, 0L);

        return result;
    }


    @Transactional
    @Override
    public CustomerMasterEntity updateCustomer(Long customerId, CustomerMasterDto dto) {
        CustomerMasterEntity entity = repository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + customerId));

        // 📌 Base Fields
        entity.setGstOrUin(dto.getGstOrUin());
        entity.setCustomerName(dto.getCustomerName());
        entity.setMailingBillingName(dto.getMailingBillingName());
        entity.setCustomerNickname(dto.getCustomerNickname());
        entity.setCustomerCategory(dto.getCustomerCategory());
        entity.setMultipleAddress(dto.getMultipleAddress());
        entity.setCreditLimitAmount(dto.getCreditLimitAmount());
        entity.setCreditLimitDays(dto.getCreditLimitDays());
        entity.setLockPeriodDays(dto.getLockPeriodDays());
        entity.setPan(dto.getPan());
        entity.setTanNumber(dto.getTanNumber());
        entity.setUdyamNumber(dto.getUdyamNumber());
        entity.setIecCode(dto.getIecCode());
        entity.setGstCertificateLink(dto.getGstCertificateLink());
        entity.setOtherDocuments(dto.getOtherDocuments());
        entity.setTypeOfIndustry(dto.getTypeOfIndustry());
        entity.setApplicationOfIndustry(dto.getApplicationOfIndustry());
        entity.setRateOfInterest(dto.getRateOfInterest());
        entity.setInterestCalculation(dto.getInterestCalculation());
        entity.setIsIecAvailable(dto.getIsIecAvailable());
        entity.setIsTanAvailable(dto.getIsTanAvailable());
        entity.setIsUdyamAvailable(dto.getIsUdyamAvailable());
        entity.setGstStateCode(dto.getGstStateCode());
        entity.setRelatedStatus(dto.getRelatedStatus());
        entity.setPanFileUpload(dto.getPanFileUpload());
        entity.setUdyamFileUpload(dto.getUdyamFileUpload());
        entity.setStatus("PENDING_APPROVAL");
        entity.setTypeOfEntity(dto.getTypeOfEntity());

        // 🧹 Clear + rebuild AddressDetails
        entity.getAddressDetails().clear();
        List<CustomerAddressEntity> updatedAddressList = dto.getAddressDetails().stream().map(adDto -> {
            CustomerAddressEntity addressEntity = new CustomerAddressEntity();
            addressEntity.setBranchName(adDto.getBranchName());
            addressEntity.setAddress(adDto.getAddress());
            addressEntity.setSupplierArea(adDto.getSupplierArea());
            addressEntity.setState(adDto.getState());
            addressEntity.setCountry(adDto.getCountry());
            addressEntity.setPincode(adDto.getPincode());
            addressEntity.setMapLocation(adDto.getMapLocation());
            addressEntity.setPrimary(adDto.getPrimary());
            addressEntity.setCustomer(entity);
            return addressEntity;
        }).collect(Collectors.toList());
        entity.getAddressDetails().addAll(updatedAddressList);

        // 🧹 Clear + rebuild ContactDetails
        entity.getContactDetails().clear();
        List<CustomerContactEntity> updatedContactList = dto.getContactDetails().stream().map(cdDto -> {
            CustomerContactEntity contactEntity = new CustomerContactEntity();
            contactEntity.setName(cdDto.getName());
            contactEntity.setDesignation(cdDto.getDesignation());
            contactEntity.setPhoneNumber(cdDto.getPhoneNumber());
            contactEntity.setEmailId(cdDto.getEmailId());
            contactEntity.setPrimary(cdDto.getPrimary());
            contactEntity.setCustomer(entity);
            return contactEntity;
        }).collect(Collectors.toList());
        entity.getContactDetails().addAll(updatedContactList);

        // 🧹 Clear + rebuild BankDetails
        entity.getBankDetails().clear();
        List<CustomerBankDetailEntity> updatedBankList = dto.getBankDetails().stream().map(bdDto -> {
            CustomerBankDetailEntity bankEntity = new CustomerBankDetailEntity();
            bankEntity.setBeneficiaryName(bdDto.getBeneficiaryName());
            bankEntity.setAccountNumber(bdDto.getAccountNumber());
            bankEntity.setBankName(bdDto.getBankName());
            bankEntity.setBranchAddress(bdDto.getBranchAddress());
            bankEntity.setAccountType(bdDto.getAccountType());
            bankEntity.setIfscCode(bdDto.getIfscCode());
            bankEntity.setMicrCode(bdDto.getMicrCode());
            bankEntity.setSwiftCode(bdDto.getSwiftCode());
            bankEntity.setBankCountry(bdDto.getBankCountry());
            bankEntity.setUpiId(bdDto.getUpiId());
            bankEntity.setPrimary(bdDto.getPrimary());
            bankEntity.setCustomer(entity);
            return bankEntity;
        }).collect(Collectors.toList());
        entity.getBankDetails().addAll(updatedBankList);

        return repository.save(entity);
    }


    @Override
    public boolean deleteCustomer(Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Page<CustomerMasterEntity> getAllCustomers(Pageable pageable) {
        return repository.findAll(pageable);

    }


    @Override
    public CustomerMasterEntity getCustomerById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
    }

    private String generateCustomerCode() {

        String lastCode = repository.findMaxCustomerCode(); // MECU0008

        int nextNumber = 1;

        if (lastCode != null && lastCode.startsWith("MECU")) {
            String numericPart = lastCode.substring(4); // 0008
            nextNumber = Integer.parseInt(numericPart) + 1; // 9
        }

        return String.format("MECU%04d", nextNumber); // MECU0009
    }

    @Override
    public boolean isCustomerNameExists(String customerName) {
        return repository.existsByCustomerNameIgnoreCase(customerName.trim());
    }

    @Override
    public List<CustomerMasterEntity> getAllCustomersWithoutPagination() {
        return repository.findAll();
    }

    @Override
    public CustomerMasterEntity approveCustomer(Long id) throws Exception {
        CustomerMasterEntity customer = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + id));
        customer.setStatus("APPROVED");
        CustomerMasterEntity approved = repository.save(customer);

        approvalWorkflowService.approve("CustomerMaster", id, customer.getCustomerCode(),
                "CUSTOMER_MASTER", "ADMIN", null, null);

        return approved;
    }

    @Override
    public CustomerMasterEntity rejectCustomer(Long id) throws Exception {
        CustomerMasterEntity customer = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + id));
        customer.setStatus("REJECTED");
        CustomerMasterEntity rejected = repository.save(customer);

        approvalWorkflowService.reject("CustomerMaster", id, customer.getCustomerCode(),
                "CUSTOMER_MASTER", "ADMIN", "Customer rejected", null);

        return rejected;
    }

    /**
     * FRD Credit Check: Validates if customer has available credit.
     * Used before SO creation to enforce credit limits.
     * @param customerId The customer ID
     * @param orderAmount The new order amount to check against credit limit
     * @return true if credit is available
     */
    public boolean isCreditAvailable(Long customerId, Double orderAmount) {
        CustomerMasterEntity customer = repository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found: " + customerId));

        if (!"APPROVED".equalsIgnoreCase(customer.getStatus())) {
            return false; // Only approved customers can place orders
        }

        Double creditLimit = customer.getCreditLimitAmount();
        if (creditLimit == null || creditLimit <= 0) {
            return true; // No credit limit = unlimited credit
        }

        // TODO: Calculate outstanding from ledger/invoices
        // For now, check against credit limit directly
        return orderAmount <= creditLimit;
    }

    /**
     * Get customer outstanding amount (total unpaid invoices).
     */
    public Double getCustomerOutstanding(Long customerId) {
        // TODO: Integrate with ledger/invoice system to calculate actual outstanding
        return 0.0;
    }

    /**
     * Get only approved customers for SO creation dropdowns.
     */
    public List<CustomerMasterEntity> getApprovedCustomers() {
        return repository.findAll().stream()
                .filter(c -> "APPROVED".equalsIgnoreCase(c.getStatus()))
                .collect(Collectors.toList());
    }

    /**
     * Get customers pending approval.
     */
    public List<CustomerMasterEntity> getPendingCustomers() {
        return repository.findAll().stream()
                .filter(c -> "PENDING_APPROVAL".equalsIgnoreCase(c.getStatus()) || "Pending".equalsIgnoreCase(c.getStatus()))
                .collect(Collectors.toList());
    }

}
