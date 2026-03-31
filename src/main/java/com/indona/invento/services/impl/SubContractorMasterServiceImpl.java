package com.indona.invento.services.impl;

import com.indona.invento.dao.SubContractorAddressRepository;
import com.indona.invento.dao.SubContractorMasterRepository;
import com.indona.invento.dto.SubContractorAddressDto;
import com.indona.invento.dto.SubContractorBankDto;
import com.indona.invento.dto.SubContractorContactDto;
import com.indona.invento.dto.SubContractorMasterDto;
import com.indona.invento.entities.SubContractorAddressEntity;
import com.indona.invento.entities.SubContractorBankEntity;
import com.indona.invento.entities.SubContractorContactEntity;
import com.indona.invento.entities.SubContractorMasterEntity;
import com.indona.invento.services.SubContractorMasterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SubContractorMasterServiceImpl implements SubContractorMasterService {

    @Autowired
    private SubContractorMasterRepository repository;
    
    @Autowired
    private SubContractorAddressRepository addressRepository;


    @Override
    public SubContractorMasterEntity save(SubContractorMasterDto dto) {
        String code = generateCode(); // Auto-code logic
        String gstPath = dto.getGstCertificatePath(); // File path from upload

        SubContractorMasterEntity entity = SubContractorMasterEntity.builder()
                .subContractorCode(code)
                .gstRegistrationType(dto.getGstRegistrationType())
                .supplierCategory(dto.getSupplierCategory())
                .subContractorType(dto.getSubContractorType())
                .subContractorName(dto.getSubContractorName())
                .mailingBillingName(dto.getMailingBillingName())
                .supplierNickname(dto.getSupplierNickname())
                .multipleAddresses(dto.getMultipleAddresses())
                .gstOrUin(dto.getGstOrUin())
                .pan(dto.getPan())
                .tanNumber(dto.getTanNumber())
                .udyamNumber(dto.getUdyamNumber())
                .iecCode(dto.getIecCode())
                .gstCertificatePath(gstPath)
                .otherDocumentsPath(dto.getOtherDocumentsPath())
                .interestCalculation(dto.getInterestCalculation())
                .rateOfInterest(dto.getRateOfInterest())
                .isUdyamAvailable(dto.getIsUdyamAvailable())
                .isIecAvailable(dto.getIsIecAvailable())
                .isTanAvailable(dto.getIsTanAvailable())
                .gstStateCode(dto.getGstStateCode())
                .relatedStatus(dto.getRelatedStatus())
                .panFileUpload(dto.getPanFileUpload())
                .udyamFileUpload(dto.getUdyamFileUpload())
                .typeOfEntity(dto.getTypeOfEntity())
                .status("Pending")
                .build();

        // 🔹 Address Details
        List<SubContractorAddressEntity> addressEntities = new ArrayList<>();
        if (dto.getAddressDetails() != null) {
            for (SubContractorAddressDto adDto : dto.getAddressDetails()) {
                SubContractorAddressEntity adEntity = SubContractorAddressEntity.builder()
                        .branchName(adDto.getBranchName())
                        .address(adDto.getAddress())
                        .supplierArea(adDto.getSupplierArea())
                        .state(adDto.getState())
                        .country(adDto.getCountry())
                        .pincode(adDto.getPincode())
                        .mapLocation(adDto.getMapLocation())
                        .Primary(adDto.getPrimary())
                        .subContractor(entity)
                        .build();
                addressEntities.add(adEntity);
            }
        }
        entity.setAddressDetails(addressEntities);

        // 🔹 Contact Details 📞
        List<SubContractorContactEntity> contactEntities = new ArrayList<>();
        if (dto.getContactDetails() != null) {
            for (SubContractorContactDto cdDto : dto.getContactDetails()) {
                SubContractorContactEntity cdEntity = SubContractorContactEntity.builder()
                        .name(cdDto.getName())
                        .designation(cdDto.getDesignation())
                        .phoneNumber(cdDto.getPhoneNumber())
                        .emailId(cdDto.getEmailId())
                        .Primary(cdDto.getPrimary())
                        .subContractor(entity)
                        .build();
                contactEntities.add(cdEntity);
            }
        }
        entity.setContactDetails(contactEntities);

        // 🔹 Bank Details 💰
        List<SubContractorBankEntity> bankEntities = new ArrayList<>();
        if (dto.getBankDetails() != null) {
            for (SubContractorBankDto bdDto : dto.getBankDetails()) {
                SubContractorBankEntity bdEntity = SubContractorBankEntity.builder()
                        .beneficiaryName(bdDto.getBeneficiaryName())
                        .accountNumber(bdDto.getAccountNumber())
                        .bankName(bdDto.getBankName())
                        .branchAddress(bdDto.getBranchAddress())
                        .accountType(bdDto.getAccountType())
                        .ifscCode(bdDto.getIfscCode())
                        .micrCode(bdDto.getMicrCode())
                        .swiftCode(bdDto.getSwiftCode())
                        .bankCountry(bdDto.getBankCountry())
                        .upiId(bdDto.getUpiId())
                        .Primary(bdDto.getPrimary())
                        .subContractor(entity)
                        .build();
                bankEntities.add(bdEntity);
            }
        }
        entity.setBankDetails(bankEntities);

        return repository.save(entity);
    }


    private String generateCode() {
        long count = repository.count() + 1;
        String code = String.format("MESC%04d", count);

        while (repository.existsBySubContractorCode(code)) {
            count++;
            code = String.format("MESC%04d", count);
        }

        return code;
    }

    @Override
    public SubContractorMasterEntity update(Long id, SubContractorMasterDto dto) {
        SubContractorMasterEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sub-Contractor not found"));

        // 🔄 Master field updates
        entity.setGstRegistrationType(dto.getGstRegistrationType());
        entity.setSupplierCategory(dto.getSupplierCategory());
        entity.setSubContractorType(dto.getSubContractorType());
        entity.setSubContractorName(dto.getSubContractorName());
        entity.setMailingBillingName(dto.getMailingBillingName());
        entity.setSupplierNickname(dto.getSupplierNickname());
        entity.setMultipleAddresses(dto.getMultipleAddresses());
        entity.setGstOrUin(dto.getGstOrUin());
        entity.setPan(dto.getPan());
        entity.setTanNumber(dto.getTanNumber());
        entity.setUdyamNumber(dto.getUdyamNumber());
        entity.setIecCode(dto.getIecCode());
        entity.setGstCertificatePath(dto.getGstCertificatePath());
        entity.setOtherDocumentsPath(dto.getOtherDocumentsPath());
        entity.setInterestCalculation(dto.getInterestCalculation());
        entity.setRateOfInterest(dto.getRateOfInterest());
        entity.setIsIecAvailable(dto.getIsIecAvailable());
        entity.setIsTanAvailable(dto.getIsTanAvailable());
        entity.setIsUdyamAvailable(dto.getIsUdyamAvailable());
        entity.setGstStateCode(dto.getGstStateCode());
        entity.setRelatedStatus(dto.getRelatedStatus());
        entity.setPanFileUpload(dto.getPanFileUpload());
        entity.setUdyamFileUpload(dto.getUdyamFileUpload());
        entity.setStatus("PENDING");
        entity.setTypeOfEntity(dto.getTypeOfEntity());

        // 🧹 Clear & refill Address list
        if (entity.getAddressDetails() != null) entity.getAddressDetails().clear();
        if (dto.getAddressDetails() != null) {
            for (SubContractorAddressDto adDto : dto.getAddressDetails()) {
                SubContractorAddressEntity adEntity = SubContractorAddressEntity.builder()
                        .Primary(adDto.getPrimary())
                        .branchName(adDto.getBranchName())
                        .address(adDto.getAddress())
                        .supplierArea(adDto.getSupplierArea())
                        .state(adDto.getState())
                        .country(adDto.getCountry())
                        .pincode(adDto.getPincode())
                        .mapLocation(adDto.getMapLocation())
                        .subContractor(entity)
                        .build();
                entity.getAddressDetails().add(adEntity);
            }
        }

        // 📞 Clear & refill Contact list
        if (entity.getContactDetails() != null) entity.getContactDetails().clear();
        if (dto.getContactDetails() != null) {
            for (SubContractorContactDto cdDto : dto.getContactDetails()) {
                SubContractorContactEntity cdEntity = SubContractorContactEntity.builder()
                        .Primary(cdDto.getPrimary())
                        .name(cdDto.getName())
                        .designation(cdDto.getDesignation())
                        .phoneNumber(cdDto.getPhoneNumber())
                        .emailId(cdDto.getEmailId())
                        .subContractor(entity)
                        .build();
                entity.getContactDetails().add(cdEntity);
            }
        }

        // 💰 Clear & refill Bank list
        if (entity.getBankDetails() != null) entity.getBankDetails().clear();
        if (dto.getBankDetails() != null) {
            for (SubContractorBankDto bdDto : dto.getBankDetails()) {
                SubContractorBankEntity bdEntity = SubContractorBankEntity.builder()
                        .beneficiaryName(bdDto.getBeneficiaryName())
                        .accountNumber(bdDto.getAccountNumber())
                        .bankName(bdDto.getBankName())
                        .branchAddress(bdDto.getBranchAddress())
                        .accountType(bdDto.getAccountType())
                        .ifscCode(bdDto.getIfscCode())
                        .micrCode(bdDto.getMicrCode())
                        .swiftCode(bdDto.getSwiftCode())
                        .bankCountry(bdDto.getBankCountry())
                        .Primary(bdDto.getPrimary())
                        .upiId(bdDto.getUpiId())
                        .subContractor(entity)
                        .build();
                entity.getBankDetails().add(bdEntity);
            }
        }

        return repository.save(entity);
    }


    @Override
    public boolean delete(Long id) {
        if (!repository.existsById(id)) {
            return false;
        }
        repository.deleteById(id);
        return true;
    }


    @Override
    public Page<SubContractorMasterEntity> getAll(Pageable pageable) {
        return repository.findAll(pageable);

    }


    @Override
    public SubContractorMasterEntity getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sub-Contractor not found"));
    }
    @Override
    public boolean isSubContractorNameExists(String name) {
        return repository.existsBySubContractorNameIgnoreCase(name.trim());
    }

    @Override
    public List<SubContractorMasterEntity> getAllWithoutPagination() {
        return repository.findAll();
    }
    
    
    @Override
    public List<String> getAllSubContractorCodes() {
        return repository.findAllSubContractorCodes();
    }

    @Override
    public List<String> getAllApprovedSubContractorCodes() {
        return repository.findAllApprovedSubContractorCodes();
    }

    @Override
    public List<String> getAllSubContractorNames() {
        return repository.findAllApprovedSubContractorNames();
    }


    @Override
    public Map<String, Object> getDetailsByCode(String subContractorCode) {
        SubContractorMasterEntity entity = repository.findBySubContractorCode(subContractorCode)
                .orElseThrow(() -> new RuntimeException("Sub-contractor not found for code: " + subContractorCode));

        SubContractorAddressEntity address = addressRepository.findPrimaryAddressBySubContractorCode(subContractorCode);

        Map<String, Object> response = new HashMap<>();
        response.put("subContractorCode", entity.getSubContractorCode());
        response.put("subContractorName", entity.getSubContractorName());
        response.put("gstOrUin", entity.getGstOrUin());
        response.put("gstStateCode", entity.getGstStateCode()); 

        if (address != null) {
            response.put("branchName", address.getBranchName());
            response.put("address", address.getAddress());
            response.put("supplierArea", address.getSupplierArea());
            response.put("state", address.getState());
            response.put("country", address.getCountry());
            response.put("pincode", address.getPincode());
            response.put("primary", address.getPrimary());
        } else {
            response.put("address", "No primary address found");
        }

        return response;
    }

    @Override
    public Map<String, Object> getDetailsByName(String subContractorName) {
        SubContractorMasterEntity entity = repository.findBySubContractorName(subContractorName)
                .orElseThrow(() -> new RuntimeException("Sub-contractor not found for name: " + subContractorName));

        SubContractorAddressEntity address = addressRepository.findPrimaryAddressBySubContractorName(subContractorName);

        Map<String, Object> response = new HashMap<>();
        response.put("subContractorCode", entity.getSubContractorCode());
        response.put("subContractorName", entity.getSubContractorName());
        response.put("gstOrUin", entity.getGstOrUin());
        response.put("gstStateCode", entity.getGstStateCode()); 

        if (address != null) {
            response.put("branchName", address.getBranchName());
            response.put("address", address.getAddress());
            response.put("supplierArea", address.getSupplierArea());
            response.put("state", address.getState());
            response.put("country", address.getCountry());
            response.put("pincode", address.getPincode());
            response.put("primary", address.getPrimary());
        } else {
            response.put("address", "No primary address found");
        }

        return response;
    }

    @Override
    public SubContractorMasterEntity approveSubContractor(Long id) throws Exception {
        SubContractorMasterEntity subContractor = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("SubContractor not found with ID: " + id));
        subContractor.setStatus("APPROVED");
        return repository.save(subContractor);
    }

    @Override
    public SubContractorMasterEntity rejectSubContractor(Long id) throws Exception {
        SubContractorMasterEntity subContractor = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("SubContractor not found with ID: " + id));
        subContractor.setStatus("REJECTED");
        return repository.save(subContractor);
    }

}

