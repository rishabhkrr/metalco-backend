package com.indona.invento.services.impl;

import com.indona.invento.dao.SupplierMasterRepository;
import com.indona.invento.dto.*;
import com.indona.invento.entities.AddressDetailsEntity;
import com.indona.invento.entities.BankDetailsEntity;
import com.indona.invento.entities.ContactDetailsEntity;
import com.indona.invento.entities.SupplierMasterEntity;
import com.indona.invento.services.ApprovalWorkflowService;
import com.indona.invento.services.AuditLogService;
import com.indona.invento.services.SupplierMasterService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SupplierMasterServiceImpl implements SupplierMasterService {

    @Autowired
    private SupplierMasterRepository supplierMasterRepository;

    @Autowired
    private ApprovalWorkflowService approvalWorkflowService;

    @Autowired
    private AuditLogService auditLogService;

    private static final String UPLOAD_DIR = "uploads/gst_certificates/";

    @Override
    public SupplierMasterEntity saveSupplier(SupplierMasterDto dto) {
        String code = generateSupplierCode();
        String gstPath = dto.getGstCertificatePath();

        SupplierMasterEntity entity = SupplierMasterEntity.builder()
                .supplierCode(code)
                .gstRegistrationType(dto.getGstRegistrationType())
                .supplierCategory(dto.getSupplierCategory())
                .supplierType(dto.getSupplierType())
                .supplierName(dto.getSupplierName())
                .mailingBillingName(dto.getMailingBillingName())
                .supplierNickname(dto.getSupplierNickname())
                .multipleAddress(dto.getMultipleAddress())


                .gstOrUin(dto.getGstOrUin())
                .pan(dto.getPan())
                .tanNumber(dto.getTanNumber())
                .udyamNumber(dto.getUdyamNumber())
                .iecCode(dto.getIecCode())

                .isTanAvailable(dto.getIsTanAvailable())
                .isUdyamAvailable(dto.getIsUdyamAvailable())
                .isIecAvailable(dto.getIsIecAvailable())

                .gstCertificatePath(gstPath)
                .otherDocumentsPath(dto.getOtherDocumentsPath())

                .interestCalculation(dto.getInterestCalculation())
                .rateOfInterest(dto.getRateOfInterest())
                .gstStateCode(dto.getGstStateCode())
                .brand(dto.getBrand())
                .relatedStatus(dto.getRelatedStatus())
                .panFileUpload(dto.getPanFileUpload())
                .udyamFileUpload(dto.getUdyamFileUpload())
                .typeOfEntity(dto.getTypeOfEntity())
                .status("PENDING_APPROVAL")
                .build();


        // 🔹 Address Details
        List<AddressDetailsEntity> addressEntities = new ArrayList<>();
        if (dto.getAddressDetails() != null) {
            for (AddressDetailsDto adDto : dto.getAddressDetails()) {
                AddressDetailsEntity adEntity = new AddressDetailsEntity();
                adEntity.setBranchName(adDto.getBranchName());
                adEntity.setAddress(adDto.getAddress());
                adEntity.setSupplierArea(adDto.getSupplierArea());
                adEntity.setState(adDto.getState());
                adEntity.setCountry(adDto.getCountry());
                adEntity.setPincode(adDto.getPincode());
                adEntity.setMapLocation(adDto.getMapLocation());
                adEntity.setPrimary(adDto.isPrimary());
                adEntity.setSupplier(entity);
                addressEntities.add(adEntity);
            }
        }
        entity.setAddressDetails(addressEntities);

        // 🔹 Contact Details
        List<ContactDetailsEntity> contactEntities = new ArrayList<>();
        if (dto.getContactDetails() != null) {
            for (ContactDetailsDto cdDto : dto.getContactDetails()) {
                ContactDetailsEntity cdEntity = new ContactDetailsEntity();
                cdEntity.setName(cdDto.getName());
                cdEntity.setDesignation(cdDto.getDesignation());
                cdEntity.setPhoneNumber(cdDto.getPhoneNumber());
                cdEntity.setEmailId(cdDto.getEmailId());
                cdEntity.setPrimary(cdDto.isPrimary());
                cdEntity.setSupplier(entity);
                contactEntities.add(cdEntity);
            }
        }
        entity.setContactDetails(contactEntities);

        // 🔹 Bank Details 💰
        List<BankDetailsEntity> bankEntities = new ArrayList<>();
        if (dto.getBankDetails() != null) {
            for (BankDetailsDto bdDto : dto.getBankDetails()) {
                BankDetailsEntity bdEntity = new BankDetailsEntity();
                bdEntity.setBeneficiaryName(bdDto.getBeneficiaryName());
                bdEntity.setAccountNumber(bdDto.getAccountNumber());
                bdEntity.setBankName(bdDto.getBankName());
                bdEntity.setBranchAddress(bdDto.getBranchAddress());
                bdEntity.setAccountType(bdDto.getAccountType());
                bdEntity.setIfscCode(bdDto.getIfscCode());
                bdEntity.setMicrCode(bdDto.getMicrCode());
                bdEntity.setSwiftCode(bdDto.getSwiftCode());
                bdEntity.setBankCountry(bdDto.getBankCountry());
                bdEntity.setUpiId(bdDto.getUpiId());
                bdEntity.setPrimary(bdDto.isPrimary());
                bdEntity.setSupplier(entity);  // mapping supplier relation
                bankEntities.add(bdEntity);
            }
        }
        entity.setBankDetails(bankEntities);

        SupplierMasterEntity saved = supplierMasterRepository.save(entity);

        // Log creation and submit for approval
        auditLogService.logAction("CREATE", "SUPPLIER_MASTER", "SupplierMaster",
                saved.getId(), saved.getSupplierCode(), null, "PENDING_APPROVAL",
                "Supplier " + saved.getSupplierName() + " created with code " + saved.getSupplierCode(),
                "SYSTEM", null);

        approvalWorkflowService.submitForApproval("SupplierMaster", saved.getId(),
                saved.getSupplierCode(), "SUPPLIER_MASTER", "SYSTEM", null, 0L);

        return saved;
    }


    private String generateSupplierCode() {
        // Get the max code from DB like MESU0009
        String maxCode = supplierMasterRepository.findMaxSupplierCode(); // Custom query

        int nextNumber = 1;
        if (maxCode != null && maxCode.startsWith("MESU")) {
            try {
                nextNumber = Integer.parseInt(maxCode.substring(4)) + 1;
            } catch (NumberFormatException e) {
                // fallback to 1 if parsing fails
                nextNumber = 1;
            }
        }

        return String.format("MESU%04d", nextNumber);
    }


    @Override
    public void deleteSupplierById(Long supplierId) {
        SupplierMasterEntity entity = supplierMasterRepository.findById(supplierId)
                .orElseThrow(() -> new RuntimeException("Supplier not found with ID: " + supplierId));

        supplierMasterRepository.delete(entity);
    }

    @Override
    public Page<SupplierMasterEntity> getAllSuppliers(int page, int size) throws Exception {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return supplierMasterRepository.findAll(pageable);
    }



    @Override
    public SupplierMasterEntity getSupplierById(Long id) throws Exception {
        return supplierMasterRepository.findById(id)
                .orElseThrow(() -> new Exception("Supplier not found with ID: " + id));
    }

    @Transactional
    @Override
    public SupplierMasterEntity updateSupplier(Long supplierId, SupplierMasterDto dto) {
        SupplierMasterEntity entity = supplierMasterRepository.findById(supplierId)
                .orElseThrow(() -> new RuntimeException("Supplier not found with ID: " + supplierId));

        // 📌 Update fields based on latest entity structure
        entity.setGstRegistrationType(dto.getGstRegistrationType());
        entity.setSupplierCategory(dto.getSupplierCategory());
        entity.setSupplierType(dto.getSupplierType());

        entity.setSupplierName(dto.getSupplierName());
        entity.setMailingBillingName(dto.getMailingBillingName());
        entity.setSupplierNickname(dto.getSupplierNickname());
        entity.setMultipleAddress(dto.getMultipleAddress());

        entity.setGstOrUin(dto.getGstOrUin());
        entity.setPan(dto.getPan());
        entity.setTanNumber(dto.getTanNumber());
        entity.setUdyamNumber(dto.getUdyamNumber());
        entity.setIecCode(dto.getIecCode());

        entity.setStatus("PENDING_APPROVAL");
        entity.setGstCertificatePath(dto.getGstCertificatePath());
        entity.setOtherDocumentsPath(dto.getOtherDocumentsPath());

        entity.setInterestCalculation(dto.getInterestCalculation());
        entity.setRateOfInterest(dto.getRateOfInterest());
        entity.setBrand(dto.getBrand());
        entity.setIsIecAvailable(dto.getIsIecAvailable());
        entity.setIsTanAvailable(dto.getIsTanAvailable());
        entity.setGstStateCode(dto.getGstStateCode());
        entity.setIsIecAvailable(dto.getIsIecAvailable());
        entity.setRelatedStatus(dto.getRelatedStatus());
        entity.setPanFileUpload(dto.getPanFileUpload());
        entity.setUdyamFileUpload(dto.getUdyamFileUpload());
        entity.setTypeOfEntity(dto.getTypeOfEntity());

        // 🧹 Clear + rebuild AddressDetails list
        entity.getAddressDetails().clear();
        List<AddressDetailsEntity> updatedAddressList = dto.getAddressDetails().stream().map(adDto -> {
            AddressDetailsEntity adEntity = new AddressDetailsEntity();
            adEntity.setBranchName(adDto.getBranchName());
            adEntity.setAddress(adDto.getAddress());
            adEntity.setSupplierArea(adDto.getSupplierArea());
            adEntity.setState(adDto.getState());
            adEntity.setCountry(adDto.getCountry());
            adEntity.setPincode(adDto.getPincode());
            adEntity.setMapLocation(adDto.getMapLocation());
            adEntity.setPrimary(adDto.isPrimary());
            adEntity.setSupplier(entity); // 👈 back-reference set
            return adEntity;
        }).collect(Collectors.toList());
        entity.getAddressDetails().addAll(updatedAddressList);

        // 🧹 Clear + rebuild ContactDetails list
        entity.getContactDetails().clear();
        List<ContactDetailsEntity> updatedContactList = dto.getContactDetails().stream().map(cdDto -> {
            ContactDetailsEntity cdEntity = new ContactDetailsEntity();
            cdEntity.setName(cdDto.getName());
            cdEntity.setDesignation(cdDto.getDesignation());
            cdEntity.setPhoneNumber(cdDto.getPhoneNumber());
            cdEntity.setEmailId(cdDto.getEmailId());
            cdEntity.setPrimary(cdDto.isPrimary());
            cdEntity.setSupplier(entity);
            return cdEntity;
        }).collect(Collectors.toList());
        entity.getContactDetails().addAll(updatedContactList);

        // 🧹 Clear + rebuild BankDetails list
        entity.getBankDetails().clear();
        List<BankDetailsEntity> updatedBankList = dto.getBankDetails().stream().map(bdDto -> {
            BankDetailsEntity bdEntity = new BankDetailsEntity();
            bdEntity.setBeneficiaryName(bdDto.getBeneficiaryName());
            bdEntity.setAccountNumber(bdDto.getAccountNumber());
            bdEntity.setBankName(bdDto.getBankName());
            bdEntity.setBranchAddress(bdDto.getBranchAddress());
            bdEntity.setAccountType(bdDto.getAccountType());
            bdEntity.setIfscCode(bdDto.getIfscCode());
            bdEntity.setMicrCode(bdDto.getMicrCode());
            bdEntity.setSwiftCode(bdDto.getSwiftCode());
            bdEntity.setBankCountry(bdDto.getBankCountry());
            bdEntity.setUpiId(bdDto.getUpiId());
            bdEntity.setPrimary(bdDto.isPrimary());
            bdEntity.setSupplier(entity);
            return bdEntity;
        }).collect(Collectors.toList());
        entity.getBankDetails().addAll(updatedBankList);

        return supplierMasterRepository.save(entity);
    }


    @Override
    public String getSupplierCodeByName(String name) {
        return supplierMasterRepository.findCodeByName(name);
    }

    @Override
    public String getSupplierNameByCode(String code) {
        return supplierMasterRepository.findNameByCode(code);
    }

    @Override
    public List<String> getAllSupplierNames() {
        return supplierMasterRepository.findAllSupplierNames();
    }

    @Override
    public List<String> getAllSupplierCodes() {
        return supplierMasterRepository.findAllSupplierCodes();
    }

    @Override
    public Page<SupplierMasterEntity> filterByGstType(String gstType, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return supplierMasterRepository.findByGstRegistrationType(gstType, pageable);
    }

    @Override
    public Page<SupplierMasterEntity> filterByCategory(String category, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return supplierMasterRepository.findBySupplierCategory(category, pageable);
    }

    @Override
    public Page<SupplierMasterEntity> filterByName(String name, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return supplierMasterRepository.findBySupplierNameContainingIgnoreCase(name, pageable);
    }

    @Override
    public Page<SupplierMasterEntity> filterByCode(String code, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return supplierMasterRepository.findBySupplierCodeContainingIgnoreCase(code, pageable);
    }

    @Override
    public Page<SupplierMasterEntity> filterByNickname(String nickname, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return supplierMasterRepository.findBySupplierNicknameContainingIgnoreCase(nickname, pageable);
    }

    @Override
    public Page<SupplierMasterEntity> filterSuppliers(SupplierFilterRequest filter, Pageable pageable) {
        Specification<SupplierMasterEntity> spec = SupplierSpecification.buildSpecification(filter);
        return supplierMasterRepository.findAll(spec, pageable);
    }


    @Override
    public boolean isSupplierNameExists(String supplierName) {
        return supplierMasterRepository.existsBySupplierNameIgnoreCase(supplierName.trim());
    }

    @Override
    public List<Map<String, String>> getSupplierNamesAndCodesByBrand(String brand) {
        List<SupplierMasterEntity> suppliers = supplierMasterRepository.findByBrandIgnoreCase(brand);
        return suppliers.stream()
                .map(s -> Map.of(
                        "supplierName", s.getSupplierName(),
                        "supplierCode", s.getSupplierCode()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<SupplierMasterEntity> getAllSuppliersWithoutPagination() throws Exception {
        return supplierMasterRepository.findAll();
    }


    @Override
    public SupplierMasterEntity getSupplierByCode(String supplierCode) {
        return supplierMasterRepository.findBySupplierCode(supplierCode)
                .orElseThrow(() -> new RuntimeException("Supplier not found with code: " + supplierCode));
    }

    @Override
    public SupplierMasterEntity approveSupplier(Long id) throws Exception {
        SupplierMasterEntity supplier = supplierMasterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found with ID: " + id));

        String previousStatus = supplier.getStatus();
        supplier.setStatus("APPROVED");
        SupplierMasterEntity approvedSupplier = supplierMasterRepository.save(supplier);

        // Audit + Notification via workflow
        approvalWorkflowService.approve("SupplierMaster", id, supplier.getSupplierCode(),
                "SUPPLIER_MASTER", "ADMIN", null, null);

        return approvedSupplier;
    }

    @Override
    public SupplierMasterEntity rejectSupplier(Long id) throws Exception {
        SupplierMasterEntity supplier = supplierMasterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found with ID: " + id));

        supplier.setStatus("REJECTED");
        SupplierMasterEntity rejectedSupplier = supplierMasterRepository.save(supplier);

        // Audit + Notification via workflow
        approvalWorkflowService.reject("SupplierMaster", id, supplier.getSupplierCode(),
                "SUPPLIER_MASTER", "ADMIN", "Supplier rejected", null);

        return rejectedSupplier;
    }

    /**
     * Get only APPROVED suppliers — used in PO creation dropdowns.
     * Per FRD: Only approved suppliers can be used in purchase orders.
     */
    public List<SupplierMasterEntity> getApprovedSuppliers() {
        return supplierMasterRepository.findAll().stream()
                .filter(s -> "APPROVED".equalsIgnoreCase(s.getStatus()))
                .collect(Collectors.toList());
    }

    /**
     * Get suppliers pending approval.
     */
    public List<SupplierMasterEntity> getPendingSuppliers() {
        return supplierMasterRepository.findAll().stream()
                .filter(s -> "PENDING_APPROVAL".equalsIgnoreCase(s.getStatus()) || "PENDING".equalsIgnoreCase(s.getStatus()))
                .collect(Collectors.toList());
    }

}
