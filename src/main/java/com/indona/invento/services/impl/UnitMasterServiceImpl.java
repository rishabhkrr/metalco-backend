package com.indona.invento.services.impl;

import com.indona.invento.dao.RackBinMasterRepository;
import com.indona.invento.dao.UnitMasterRepository;
import com.indona.invento.dto.UnitAddressDetailsDto;
import com.indona.invento.dto.UnitBankDetailsDto;
import com.indona.invento.dto.UnitContactDetailsDto;
import com.indona.invento.dto.UnitMasterDto;
import com.indona.invento.dto.RackBinMasterDto;
import com.indona.invento.entities.RackBinMasterEntity;
import com.indona.invento.entities.UnitBankDetailsEntity;
import com.indona.invento.entities.UnitContactDetailsEntity;
import com.indona.invento.entities.UnitMasterEntity;
import com.indona.invento.entities.unitAddressEntity;
import com.indona.invento.services.UnitMasterService;
import com.indona.invento.services.RackBinMasterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UnitMasterServiceImpl implements UnitMasterService {

    @Autowired
    private UnitMasterRepository repository;

    @Autowired
    private RackBinMasterService rackBinMasterService;

    @Autowired
    private RackBinMasterRepository rackBinMasterRepository;

    private String generateCode() {
        long count = repository.count() + 1;
        String code = String.format("MEUN%04d", count);
        while (repository.existsByUnitCode(code)) {
            count++;
            code = String.format("MEUN%04d", count);
        }
        return code;
    }

    @Override
    public UnitMasterEntity create(UnitMasterDto dto) {
        UnitMasterEntity entity = UnitMasterEntity.builder()
                .gstRegistrationType(dto.getGstRegistrationType())
                .unitCode(generateCode())
                .unitName(dto.getUnitName())
                .unitAddress(dto.getUnitAddress())
                .area(dto.getArea())
                .state(dto.getState())
                .country(dto.getCountry())
                .pincode(dto.getPincode())
                .mapLocation(dto.getMapLocation())
                .gstOrUin(dto.getGstOrUin())
                .pan(dto.getPan())
                .tanNumber(dto.getTanNumber())
                .udyamNumber(dto.getUdyamNumber())
                .iecCode(dto.getIecCode())
                .gstCertificate(dto.getGstCertificate())
                .otherDocuments(dto.getOtherDocuments())
                .isUdyamAvailable(dto.getIsUdyamAvailable())
                .isIecAvailable(dto.getIsIecAvailable())
                .isTanAvailable(dto.getIsTanAvailable())
                .gstStateCode(dto.getGstStateCode())
                .typeOfEntity(dto.getTypeOfEntity())
                .status("Pending")
                .build();

        // 🔹 Contact Details Mapping 📞
        List<UnitContactDetailsEntity> contactEntities = new ArrayList<>();
        if (dto.getContactDetails() != null) {
            for (UnitContactDetailsDto cdDto : dto.getContactDetails()) {
                UnitContactDetailsEntity cdEntity = new UnitContactDetailsEntity();
                cdEntity.setName(cdDto.getName());
                cdEntity.setDesignation(cdDto.getDesignation());
                cdEntity.setPhoneNumber(cdDto.getPhoneNumber());
                cdEntity.setEmailId(cdDto.getEmailId());
                cdEntity.setPrimary(cdDto.getPrimary());
                cdEntity.setUnit(entity); // Set reverse mapping
                contactEntities.add(cdEntity);
            }
        }
        entity.setContactDetails(contactEntities);

        // 🔹 Bank Details Mapping 💰
        List<UnitBankDetailsEntity> bankEntities = new ArrayList<>();
        if (dto.getBankDetails() != null) {
            for (UnitBankDetailsDto bdDto : dto.getBankDetails()) {
                UnitBankDetailsEntity bdEntity = new UnitBankDetailsEntity();
                bdEntity.setBeneficiaryName(bdDto.getBeneficiaryName());
                bdEntity.setAccountNumber(bdDto.getAccountNumber());
                bdEntity.setBankName(bdDto.getBankName());
                bdEntity.setBranchAddress(bdDto.getBranchAddress());
                bdEntity.setAccountType(bdDto.getAccountType());
                bdEntity.setIfscCode(bdDto.getIfscCode());
                bdEntity.setMicrCode(bdDto.getMicrCode());
                bdEntity.setPrimary(bdDto.getPrimary());
                bdEntity.setSwiftCode(bdDto.getSwiftCode());
                bdEntity.setBankCountry(bdDto.getBankCountry());
                bdEntity.setUpiId(bdDto.getUpiId());
                bdEntity.setUnit(entity); // Reverse mapping
                bankEntities.add(bdEntity);
            }
        }
        entity.setBankDetails(bankEntities);

        // 🔹 Address Details Mapping 📍
        List<unitAddressEntity> addressEntities = new ArrayList<>();
        if (dto.getAddressDetails() != null) {
            for (UnitAddressDetailsDto adDto : dto.getAddressDetails()) {
                unitAddressEntity adEntity = new unitAddressEntity();
                adEntity.setPrimary(adDto.getPrimary());
                adEntity.setBranchName(adDto.getBranchName());
                adEntity.setAddress(adDto.getAddress());
                adEntity.setSupplierArea(adDto.getSupplierArea()); // Mapping supplierArea to entity's area
                adEntity.setState(adDto.getState());
                adEntity.setCountry(adDto.getCountry());
                adEntity.setPincode(adDto.getPincode());
                adEntity.setMapLocation(adDto.getMapLocation());
                adEntity.setUnit(entity); // Reverse mapping
                addressEntities.add(adEntity);
            }
        }
        entity.setAddressDetails(addressEntities);

        // 🔄 Save and return
        UnitMasterEntity savedUnit = repository.save(entity);

        // ✅ Default racks are created upon APPROVAL, not at creation time
        return savedUnit;
    }

    // Method to create 7 default warehouse columns
    private void createDefaultWarehouseColumns(UnitMasterEntity unit) {
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║   🏢 CREATING DEFAULT WAREHOUSE COLUMNS FOR NEW UNIT          ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        System.out.println("\n📋 Unit Details:");
        System.out.println("   ├─ Unit Code: " + unit.getUnitCode());
        System.out.println("   └─ Unit Name: " + unit.getUnitName());

        String[][] warehouses = {
            {"Warehouse", "Common"},
            {"Production", "Common"},
            {"Dispatch", "Common"},
            {"Loose Piece Storage", "Common"},
            {"End Piece Storage", "Common"},
            {"Rejection", "Rejection"},
            {"Warehouse", "Scrap"}
        };

        System.out.println("\n📦 Creating 7 Warehouse Columns:");
        System.out.println("   ─────────────────────────────────────────\n");

        int count = 1;
        for (String[] warehouse : warehouses) {
            System.out.println("   [" + count + "/7] Creating warehouse column...");
            System.out.println("       ├─ Storage Type: " + warehouse[0]);
            System.out.println("       ├─ Storage Area: " + warehouse[1]);
            System.out.println("       ├─ Rack No: (empty)");
            System.out.println("       ├─ Column No: (empty)");
            System.out.println("       ├─ Bin No: Common Bin");
            System.out.println("       └─ Status: Pending");

            RackBinMasterDto rackBinDto = RackBinMasterDto.builder()
                    .unitCode(unit.getUnitCode())
                    .unitName(unit.getUnitName())
                    .storageType(warehouse[0])
                    .storageArea(warehouse[1])
                    .rackNo(null)         // Empty by default
                    .columnNo(null)       // Empty by default
                    .binNo("Common Bin")   // Default "Common Bin"
                    .status("APPROVED")
                    .binCapacity(String.valueOf(Double.MAX_VALUE))
                    .distance((double) 0)
                    .itemCategory("ALL")
                    .storageAreaOrder(0)
                    .automated(true)
                    .currentStorage((double) 0)

                    .build();

            try {
                rackBinMasterService.create(rackBinDto);
                System.out.println("       ✅ Successfully created!\n");
            } catch (Exception e) {
                System.err.println("       ❌ Error: " + e.getMessage());
                System.err.println("       Error Stack: " + e.getClass().getSimpleName() + "\n");
            }
            count++;
        }

        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║   ✅ WAREHOUSE COLUMNS CREATION COMPLETED                    ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝\n");
    }


    @Override
    public UnitMasterEntity update(Long id, UnitMasterDto dto) {
        UnitMasterEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Unit not found"));

        // 🔄 Basic field updates
        entity.setGstRegistrationType(dto.getGstRegistrationType());
        entity.setUnitName(dto.getUnitName());
        entity.setUnitAddress(dto.getUnitAddress());
        entity.setArea(dto.getArea());
        entity.setState(dto.getState());
        entity.setCountry(dto.getCountry());
        entity.setPincode(dto.getPincode());
        entity.setMapLocation(dto.getMapLocation());
        entity.setGstOrUin(dto.getGstOrUin());
        entity.setPan(dto.getPan());
        entity.setTanNumber(dto.getTanNumber());
        entity.setUdyamNumber(dto.getUdyamNumber());
        entity.setIecCode(dto.getIecCode());
        entity.setGstCertificate(dto.getGstCertificate());
        entity.setOtherDocuments(dto.getOtherDocuments());
        entity.setIsIecAvailable(dto.getIsIecAvailable());
        entity.setIsTanAvailable(dto.getIsTanAvailable());
        entity.setIsIecAvailable(dto.getIsIecAvailable());
        entity.setGstStateCode(dto.getGstStateCode());
        entity.setTypeOfEntity(dto.getTypeOfEntity());

        // 📞 Clear and refill Contact Details
        if (entity.getContactDetails() != null) {
            entity.getContactDetails().clear();
        }
        if (dto.getContactDetails() != null) {
            for (UnitContactDetailsDto cdDto : dto.getContactDetails()) {
                UnitContactDetailsEntity cdEntity = new UnitContactDetailsEntity();
                cdEntity.setName(cdDto.getName());
                cdEntity.setDesignation(cdDto.getDesignation());
                cdEntity.setPhoneNumber(cdDto.getPhoneNumber());
                cdEntity.setEmailId(cdDto.getEmailId());
                cdEntity.setPrimary(cdDto.getPrimary());
                cdEntity.setUnit(entity);
                entity.getContactDetails().add(cdEntity); // ✅ don't replace collection
            }
        }

        // 💰 Clear and refill Bank Details
        if (entity.getBankDetails() != null) {
            entity.getBankDetails().clear();
        }
        if (dto.getBankDetails() != null) {
            for (UnitBankDetailsDto bdDto : dto.getBankDetails()) {
                UnitBankDetailsEntity bdEntity = new UnitBankDetailsEntity();
                bdEntity.setBeneficiaryName(bdDto.getBeneficiaryName());
                bdEntity.setAccountNumber(bdDto.getAccountNumber());
                bdEntity.setBankName(bdDto.getBankName());
                bdEntity.setBranchAddress(bdDto.getBranchAddress());
                bdEntity.setAccountType(bdDto.getAccountType());
                bdEntity.setIfscCode(bdDto.getIfscCode());
                bdEntity.setMicrCode(bdDto.getMicrCode());
                bdEntity.setPrimary(bdDto.getPrimary());
                bdEntity.setSwiftCode(bdDto.getSwiftCode());
                bdEntity.setBankCountry(bdDto.getBankCountry());
                bdEntity.setUpiId(bdDto.getUpiId());
                bdEntity.setUnit(entity);
                entity.getBankDetails().add(bdEntity); // ✅ direct add
            }
        }

        return repository.save(entity);
    }


    @Override
    public boolean delete(Long id) {
        if (!repository.existsById(id)) return false;
        repository.deleteById(id);
        return true;
    }

    @Override
    public Page<UnitMasterEntity> getAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    public UnitMasterEntity getById(Long id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Unit not found"));
    }

    @Override
    public UnitMasterEntity getByUnitCode(String unitCode) {
        return repository.findByUnitCode(unitCode)
                .orElseThrow(() -> new RuntimeException("Unit not found with code: " + unitCode));
    }

    @Override
    public UnitMasterEntity getByUnitName(String unitName) {
        return repository.findByUnitName(unitName)
                .orElseThrow(() -> new RuntimeException("Unit not found with name: " + unitName));
    }

    @Override
    public List<String> getAllUnitCodes() {
        List<UnitMasterEntity> units = repository.findAll();
        return units.stream()
                .map(UnitMasterEntity::getUnitCode)
                .toList();
    }

    @Override
    public List<String> getAllApprovedUnitCodes() {
        return repository.findAllApprovedUnitCodes();
    }

    @Override
    public List<UnitMasterEntity> getAllWithoutPagination() {
        return repository.findAll();
    }


    @Override
    public List<String> getUnitCodesByName(String unitName) {
        return repository.findUnitCodesByName(unitName);
    }

    @Override
    public UnitMasterEntity approveUnit(Long id) throws Exception {
        UnitMasterEntity unit = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Unit not found with ID: " + id));
        unit.setStatus("APPROVED");
        UnitMasterEntity savedUnit = repository.save(unit);

        // ✅ AUTO-CREATE 7 DEFAULT RACKS on approval (only if not already created)
        List<RackBinMasterEntity> existing = rackBinMasterRepository.findByStorageType("Warehouse");
        boolean alreadyHasDefaults = existing.stream()
                .anyMatch(r -> savedUnit.getUnitCode().equalsIgnoreCase(r.getUnitCode()) && r.isAutomated());
        if (!alreadyHasDefaults) {
            createDefaultWarehouseColumns(savedUnit);
        }

        return savedUnit;
    }

    @Override
    public UnitMasterEntity rejectUnit(Long id) throws Exception {
        UnitMasterEntity unit = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Unit not found with ID: " + id));
        unit.setStatus("REJECTED");
        return repository.save(unit);
    }

}
