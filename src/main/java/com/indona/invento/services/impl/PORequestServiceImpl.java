package com.indona.invento.services.impl;

import com.indona.invento.dao.PORequestRepository;
import com.indona.invento.dao.UnitMasterRepository;
import com.indona.invento.dao.UserMasterRepository;
import com.indona.invento.dto.PORequestDTO;
import com.indona.invento.dto.SupplierCodeNameDTO;
import com.indona.invento.dto.UnitNameCodeDTO;
import com.indona.invento.dto.PORequestDetailsDto;
import com.indona.invento.entities.POProductEntity;
import com.indona.invento.entities.PORequestEntity;
import com.indona.invento.entities.UserMasterEntity;
import com.indona.invento.services.PORequestService;
import jakarta.transaction.Transactional;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
@Service
public class PORequestServiceImpl implements PORequestService {

    @Autowired
    private PORequestRepository repository;


    @Autowired
    private UnitMasterRepository unitMasterRepository;

    @Autowired
    private UserMasterRepository userMasterRepository;


    @Override
    public PORequestEntity createPORequest(PORequestDTO dto) {
        PORequestEntity entity = mapToEntity(dto);
        entity.setTimeStamp(new Date());
        entity.setStatus("PENDING");
        entity.setPrNumber(generatePRNumber());
        return repository.save(entity);
    }

    @Override
    public PORequestEntity updatePORequest(Long id, PORequestDTO dto) {
        PORequestEntity existing = repository.findById(id).orElseThrow();
        updateEntityFromDTO(existing, dto);
        return repository.save(existing);
    }

    @Override
    public PORequestEntity deletePORequest(Long id) {
        PORequestEntity entity = repository.findById(id).orElseThrow();
        repository.delete(entity);
        return entity;
    }

    @Override
    public PORequestEntity getPORequestById(Long id) {
        return repository.findById(id).orElseThrow();
    }

    @Override
    public List<PORequestEntity> getAllPORequests() {
        return repository.findAll();
    }

    // 🔁 Manual Mapper
    private PORequestEntity mapToEntity(PORequestDTO dto) {
        PORequestEntity entity = new PORequestEntity();
        entity.setSupplierCode(dto.getSupplierCode());
        entity.setOrderType(dto.getOrderType());
        entity.setSupplierName(dto.getSupplierName());
        entity.setUnitCode(dto.getUnitCode());
        entity.setUnit(dto.getUnit());
        entity.setSoNumberLineNumber(dto.getSoNumberLineNumber());
        entity.setPrCreatedBy(dto.getPrCreatedBy());
        entity.setReasonForRequest(dto.getReasonForRequest());

        if (dto.getProducts() != null) {
            List<POProductEntity> productEntities = dto.getProducts().stream()
                    .map(p -> POProductEntity.builder()
                            .sectionNo(p.getSectionNo())
                            .itemDescription(p.getItemDescription())
                            .productCategory(p.getProductCategory())
                            .brand(p.getBrand())
                            .grade(p.getGrade())
                            .temper(p.getTemper())
                            .requiredQuantity(p.getRequiredQuantity())
                            .uom(p.getUom())
                            .selected(p.getSelected())
                            .poRequest(entity)
                            .build())
                    .collect(Collectors.toList());

            entity.setProducts(productEntities);
        }

        return entity;
    }

    private void updateEntityFromDTO(PORequestEntity entity, PORequestDTO dto) {
        entity.setSupplierCode(dto.getSupplierCode());
        entity.setSupplierName(dto.getSupplierName());
        entity.setUnit(dto.getUnit());
        entity.setOrderType(dto.getOrderType());
        entity.setUnitCode(dto.getUnitCode());
        entity.setSoNumberLineNumber(dto.getSoNumberLineNumber());
        entity.setPrCreatedBy(dto.getPrCreatedBy());
        entity.setReasonForRequest(dto.getReasonForRequest());

        if (dto.getStatus() != null) {
            entity.setStatus(dto.getStatus());
        }

        // 🔁 Clear old products and add new ones
        entity.getProducts().clear();

        if (dto.getProducts() != null) {
            List<POProductEntity> updatedProducts = dto.getProducts().stream()
                    .map(p -> POProductEntity.builder()
                            .sectionNo(p.getSectionNo())
                            .itemDescription(p.getItemDescription())
                            .productCategory(p.getProductCategory())
                            .brand(p.getBrand())
                            .grade(p.getGrade())
                            .temper(p.getTemper())
                            .selected(p.getSelected())
                            .requiredQuantity(p.getRequiredQuantity())
                            .uom(p.getUom())
                            .poRequest(entity) // 🔗 link back to parent
                            .build())
                    .collect(Collectors.toList());

            entity.getProducts().addAll(updatedProducts);
        }
    }


    // 🔢 PR Number Generator
    @Transactional
    public String generatePRNumber() {
        String prefix = "MEPR";
        String year = new SimpleDateFormat("yy").format(new Date());
        String month = new SimpleDateFormat("MM").format(new Date());
        String base = prefix + year + month;

        // Locking to prevent race conditions
        synchronized (this) {
            int count = repository.countByPrefix(base);
            String sequence = String.format("%04d", count + 1);
            String prNumber = base + sequence;

            // Double-check in DB to avoid duplicates
            while (repository.existsByPrNumber(prNumber)) {
                count++;
                sequence = String.format("%04d", count + 1);
                prNumber = base + sequence;
            }

            return prNumber;
        }
    }


    @Override
    public List<PORequestEntity> getPOsBySupplier(String supplierCode, String supplierName, String unitCode) {
        return repository.findBySupplierAndUnitCode(supplierCode, supplierName, unitCode);
    }



    @Override
    public List<SupplierCodeNameDTO> getAllSupplierCodeNamePairs() {
        return repository.findAllDistinctSupplierCodeNamePairs();
    }


    @Override
    public List<UnitNameCodeDTO> getAllUnitNamesWithCodes() {
        List<String> unitNames = repository.findAllDistinctUnits();

        return unitNames.stream()
                .filter(name -> name != null && !name.trim().isEmpty()) // ⛔️ Skip blank names
                .map(name -> {
                    String code = unitMasterRepository.findUnitCodeByUnitName(name).orElse("N/A");
                    return new UnitNameCodeDTO(name, code);
                })
                .filter(dto -> dto.getUnitCode() != null && !dto.getUnitCode().equals("N/A")) // ⛔️ Skip unmatched codes
                .collect(Collectors.toList());
    }

    @Override
    public List<PORequestEntity> getPORequestsBetweenDates(Date fromDate, Date toDate) {
        return repository.findByTimeStampBetween(fromDate, toDate);
    }

    @Override
    public PORequestDetailsDto getPORequestDetailsByPrNumber(String prNumber) {
        // Fetch the PO Request by PR Number
        PORequestEntity poRequest = repository.findByPrNumber(prNumber)
                .orElseThrow(() -> new RuntimeException("PO Request not found with PR Number: " + prNumber));

        // Get the created by user name
        String createdBy = poRequest.getPrCreatedBy();
        String reasonForRequest = poRequest.getReasonForRequest();

        // Fetch the user from UserMaster using the created by name
        UserMasterEntity user = userMasterRepository.findByUserName(createdBy);
        String userDepartment = "N/A";

        if (user != null) {
            userDepartment = user.getDepartment() != null ? user.getDepartment() : "N/A";
        }

        // Build and return the DTO
        return PORequestDetailsDto.builder()
                .prNumber(poRequest.getPrNumber())
                .createdBy(createdBy)
                .reasonForRequest(reasonForRequest)
                .userDepartment(userDepartment)
                .build();
    }

    @Override
    public void deleteAllPORequests() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║      🗑️  DELETE ALL PO REQUESTS       ║");
        System.out.println("╚════════════════════════════════════════╝\n");

        try {
            long totalCount = repository.count();
            System.out.println("📊 Total PO requests before deletion: " + totalCount);

            repository.deleteAll();

            long afterCount = repository.count();
            System.out.println("✅ All PO requests deleted successfully!");
            System.out.println("📊 Total PO requests after deletion: " + afterCount);
            System.out.println("\n╔════════════════════════════════════════╗");
            System.out.println("║     ✅ DELETION COMPLETE               ║");
            System.out.println("╚════════════════════════════════════════╝\n");
        } catch (Exception e) {
            System.err.println("❌ Error deleting all PO requests: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to delete all PO requests: " + e.getMessage());
        }
    }
}
