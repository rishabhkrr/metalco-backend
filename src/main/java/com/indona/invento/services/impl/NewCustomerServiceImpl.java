package com.indona.invento.services.impl;

import com.indona.invento.dao.CustomerMasterRepository;
import com.indona.invento.dao.ItemEnquiryRepository;
import com.indona.invento.dao.NewCustomerDetailsRepository;
import com.indona.invento.dto.NewCustomerDto;
import com.indona.invento.entities.*;

import com.indona.invento.services.NewCustomerService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class NewCustomerServiceImpl implements NewCustomerService {

    @Autowired
    private NewCustomerDetailsRepository repository;

    @Autowired
    private CustomerMasterRepository customerMasterRepository;

    @Autowired
    private ItemEnquiryRepository itemEnquiryRepository;

    @Override
    public NewCustomerDetails createNewCustomer(NewCustomerDto dto) {


        NewCustomerDetails customer = NewCustomerDetails.builder()
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
                .customerBased(dto.getCustomerBased())
                .status("PENDING") // ✅ Hardcoded string
                .build();

        NewCustomerDetails saved = repository.save(customer);

        List<NewCustomerAddessEntity> addressEntities = dto.getAddressDetails().stream()
                .map(addr -> NewCustomerAddessEntity.builder()
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

        List<NewCustomerContactEntity> contactEntities = dto.getContactDetails().stream()
                .map(contact -> NewCustomerContactEntity.builder()
                        .Primary(contact.getPrimary())
                        .name(contact.getName())
                        .designation(contact.getDesignation())
                        .phoneNumber(contact.getPhoneNumber())
                        .emailId(contact.getEmailId())
                        .customer(saved)
                        .build())
                .collect(Collectors.toCollection(ArrayList::new));
        saved.setContactDetails(contactEntities);

        List<NewCustomerBankDetailsEntity> bankEntities = dto.getBankDetails().stream()
                .map(bank -> NewCustomerBankDetailsEntity.builder()
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

        return repository.save(saved);
    }

//    private String generateCustomerCode() {
//        String datePart = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
//        long count = repository.count() + 1;
//        return String.format("MECU-%s-%03d", datePart, count);
//    }

    @Override
    public List<NewCustomerDetails> getAllCustomers() {
        return repository.findAll();
    }

    @Override
    public NewCustomerDetails getCustomerById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + id));
    }

    @Override
    public NewCustomerDetails deleteCustomer(Long id) {
        return repository.findById(id)
                .map(customer -> {
                    repository.delete(customer); // ✅ Cascade delete will trigger
                    return customer;
                })
                .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + id));
    }

    @Override
    public void deleteAllCustomers() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║     🗑️  DELETE ALL CUSTOMERS           ║");
        System.out.println("╚════════════════════════════════════════╝\n");

        try {
            long totalCount = repository.count();
            System.out.println("📊 Total customers before deletion: " + totalCount);

            repository.deleteAll();

            long afterCount = repository.count();
            System.out.println("✅ All customers deleted successfully!");
            System.out.println("📊 Total customers after deletion: " + afterCount);
            System.out.println("\n╔════════════════════════════════════════╗");
            System.out.println("║     ✅ DELETION COMPLETE               ║");
            System.out.println("╚════════════════════════════════════════╝\n");
        } catch (Exception e) {
            System.err.println("❌ Error deleting all customers: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to delete all customers: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public NewCustomerDetails updateCustomer(Long id, NewCustomerDto dto) {
        log.info("🔄 Updating customer with ID: {}", id);

        NewCustomerDetails existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + id));

        log.info("✅ Customer found: {}", existing.getCustomerName());

        // Update basic fields
        existing.setGstRegistrationType(dto.getGstRegistrationType());
        existing.setGstOrUin(dto.getGstOrUin());
        existing.setCustomerCategory(dto.getCustomerCategory());
        existing.setCustomerName(dto.getCustomerName());
        existing.setMailingBillingName(dto.getMailingBillingName());
        existing.setCustomerNickname(dto.getCustomerNickname());
        existing.setMultipleAddress(dto.getMultipleAddress());
        existing.setCreditLimitAmount(dto.getCreditLimitAmount());
        existing.setCreditLimitDays(dto.getCreditLimitDays());
        existing.setLockPeriodDays(dto.getLockPeriodDays());
        existing.setPan(dto.getPan());
        existing.setTanNumber(dto.getTanNumber());
        existing.setUdyamNumber(dto.getUdyamNumber());
        existing.setIecCode(dto.getIecCode());
        existing.setGstCertificateLink(dto.getGstCertificateLink());
        existing.setOtherDocuments(dto.getOtherDocuments());
        existing.setTypeOfIndustry(dto.getTypeOfIndustry());
        existing.setApplicationOfIndustry(dto.getApplicationOfIndustry());
        existing.setInterestCalculation(dto.getInterestCalculation());
        existing.setRateOfInterest(dto.getRateOfInterest());
        existing.setIsUdyamAvailable(dto.getIsUdyamAvailable());
        existing.setIsTanAvailable(dto.getIsTanAvailable());
        existing.setIsIecAvailable(dto.getIsIecAvailable());
        existing.setStatus(dto.getStatus());
        existing.setGstStateCode(dto.getGstStateCode());

        log.info("🧹 Clearing and updating address, contact, and bank details");

        existing.getAddressDetails().clear();
        dto.getAddressDetails().forEach(addr -> {
            existing.getAddressDetails().add(NewCustomerAddessEntity.builder()
                    .Primary(addr.getPrimary())
                    .branchName(addr.getBranchName())
                    .address(addr.getAddress())
                    .supplierArea(addr.getSupplierArea())
                    .state(addr.getState())
                    .country(addr.getCountry())
                    .pincode(addr.getPincode())
                    .mapLocation(addr.getMapLocation())
                    .customer(existing)
                    .build());
        });

        existing.getContactDetails().clear();
        dto.getContactDetails().forEach(contact -> {
            existing.getContactDetails().add(NewCustomerContactEntity.builder()
                    .Primary(contact.getPrimary())
                    .name(contact.getName())
                    .designation(contact.getDesignation())
                    .phoneNumber(contact.getPhoneNumber())
                    .emailId(contact.getEmailId())
                    .customer(existing)
                    .build());
        });

        existing.getBankDetails().clear();
        dto.getBankDetails().forEach(bank -> {
            existing.getBankDetails().add(NewCustomerBankDetailsEntity.builder()
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
                    .customer(existing)
                    .build());
        });

        NewCustomerDetails saved = repository.save(existing);
        log.info("💾 Customer details saved successfully");

        long count = customerMasterRepository.count() + 1;
        String customerCode = String.format("MECU%04d", count);
        while (customerMasterRepository.existsByCustomerCode(customerCode)) {
            count++;
            customerCode = String.format("MECU%04d", count);
        }

        saved.setCustomerCode(customerCode);
        repository.save(saved);
        log.info("🆕 Generated and assigned customerCode: {}", customerCode);

        // Sync to CustomerMasterEntity
        CustomerMasterEntity master = CustomerMasterEntity.builder()
                .customerCode(customerCode)
                .gstRegistrationType(saved.getGstRegistrationType())
                .gstOrUin(saved.getGstOrUin())
                .customerCategory(saved.getCustomerCategory())
                .customerName(saved.getCustomerName())
                .mailingBillingName(saved.getMailingBillingName())
                .customerNickname(saved.getCustomerNickname())
                .multipleAddress(saved.getMultipleAddress())
                .creditLimitAmount(saved.getCreditLimitAmount())
                .creditLimitDays(saved.getCreditLimitDays())
                .lockPeriodDays(saved.getLockPeriodDays())
                .pan(saved.getPan())
                .tanNumber(saved.getTanNumber())
                .udyamNumber(saved.getUdyamNumber())
                .iecCode(saved.getIecCode())
                .gstCertificateLink(saved.getGstCertificateLink())
                .otherDocuments(saved.getOtherDocuments())
                .typeOfIndustry(saved.getTypeOfIndustry())
                .applicationOfIndustry(saved.getApplicationOfIndustry())
                .interestCalculation(saved.getInterestCalculation())
                .rateOfInterest(saved.getRateOfInterest())
                .isUdyamAvailable(saved.getIsUdyamAvailable())
                .isTanAvailable(saved.getIsTanAvailable())
                .isIecAvailable(saved.getIsIecAvailable())
                .gstStateCode(saved.getGstStateCode())
                .build();

        master.setAddressDetails(saved.getAddressDetails().stream()
                .map(addr -> CustomerAddressEntity.builder()
                        .Primary(addr.getPrimary())
                        .branchName(addr.getBranchName())
                        .address(addr.getAddress())
                        .supplierArea(addr.getSupplierArea())
                        .state(addr.getState())
                        .country(addr.getCountry())
                        .pincode(addr.getPincode())
                        .mapLocation(addr.getMapLocation())
                        .customer(master)
                        .build())
                .collect(Collectors.toList()));

        master.setContactDetails(saved.getContactDetails().stream()
                .map(contact -> CustomerContactEntity.builder()
                        .Primary(contact.getPrimary())
                        .name(contact.getName())
                        .designation(contact.getDesignation())
                        .phoneNumber(contact.getPhoneNumber())
                        .emailId(contact.getEmailId())
                        .customer(master)
                        .build())
                .collect(Collectors.toList()));

        master.setBankDetails(saved.getBankDetails().stream()
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
                        .customer(master)
                        .build())
                .collect(Collectors.toList()));

        customerMasterRepository.save(master);
        log.info("🔁 Synced data to CustomerMasterEntity");

        List<ItemEnquiry> enquiries = itemEnquiryRepository.findByCustomerName(saved.getCustomerName());
        String finalCustomerCode = customerCode;
        enquiries.forEach(enquiry -> enquiry.setCustomerCode(finalCustomerCode));
        itemEnquiryRepository.saveAll(enquiries);
        log.info("📦 Updated {} ItemEnquiry records with new customerCode: {}", enquiries.size(), finalCustomerCode);

        log.info("✅ Customer update process completed for ID: {}", id);
        return saved;
    }

}
