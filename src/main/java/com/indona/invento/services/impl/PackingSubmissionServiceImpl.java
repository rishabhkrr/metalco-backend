package com.indona.invento.services.impl;

import com.indona.invento.dao.PackingSchedulerRepository;
import com.indona.invento.dao.PackingSubmissionRepository;
import com.indona.invento.dto.PackingBatchDetailDTO;
import com.indona.invento.dto.PackingSubmissionDTO;
import com.indona.invento.dto.PackingSubmissionResponseDTO;
import com.indona.invento.entities.PackingBatchDetail;
import com.indona.invento.entities.PackingEntityScheduler;
import com.indona.invento.entities.PackingSubmission;

import com.indona.invento.services.PackingSubmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class PackingSubmissionServiceImpl implements PackingSubmissionService {

    @Autowired
    private PackingSubmissionRepository packingSubmissionRepository;

    @Autowired
    private PackingSchedulerRepository packingSchedulerRepository;

    @Override
    @Transactional
    public PackingSubmissionResponseDTO submitPackingForms(List<PackingSubmissionDTO> dtos) {

        String packingId = generatePackingId();
        List<PackingSubmissionResponseDTO.LineItemResponse> lineItemResponses = new ArrayList<>();

        for (PackingSubmissionDTO dto : dtos) {
            PackingSubmission submission = PackingSubmission.builder()
                    .packingId(packingId)
                    .soNumber(dto.getSoNumber())
                    .lineNumber(dto.getLineNumber())
                    .unit(dto.getUnit())
                    .customerCode(dto.getCustomerCode())
                    .customerName(dto.getCustomerName())
                    .orderType(dto.getOrderType())
                    .productCategory(dto.getProductCategory())
                    .itemDescription(dto.getItemDescription())
                    .brand(dto.getBrand())
                    .grade(dto.getGrade())
                    .temper(dto.getTemper())
                    .dimension(dto.getDimension())
                    .quantityKg(dto.getWeighmentQuantityKg())
                    .uomKg(dto.getUomKg())
                    .quantityNo(dto.getWeighmentQuantityNo())
                    .uomNo(dto.getUomNo())
                    .packingInstructions(dto.getPackingType())
                    .packingStatus("Completed")
                    .pdf(dto.getPdf())
                    .build();

            // Handle batch details if present
            if (dto.getBatchDetails() != null && !dto.getBatchDetails().isEmpty()) {
                for (PackingBatchDetailDTO batchDto : dto.getBatchDetails()) {
                    PackingBatchDetail batchDetail = PackingBatchDetail.builder()
                            .itemDescription(batchDto.getItemDescription())
                            .itemDimensions(batchDto.getItemDimensions())
                            .batchNumber(batchDto.getBatchNumber())
                            .dateOfInward(batchDto.getDateOfInward())
                            .qtyKg(batchDto.getQtyKg())
                            .qtyNo(batchDto.getQtyNo())
                            .build();
                    submission.addBatchDetail(batchDetail);
                }
            }

            PackingSubmission savedSubmission = packingSubmissionRepository.save(submission);

            lineItemResponses.add(PackingSubmissionResponseDTO.LineItemResponse.builder()
                    .id(savedSubmission.getId())
                    .soNumber(savedSubmission.getSoNumber())
                    .lineNumber(savedSubmission.getLineNumber())
                    .createdAt(savedSubmission.getCreatedAt())
                    .build());

            PackingEntityScheduler packing = packingSchedulerRepository.findBySoNumberAndLineNumber(dto.getSoNumber(),
                    dto.getLineNumber());
            if (packing != null) {
                packing.setPackingStatus("COMPLETE");
                packingSchedulerRepository.save(packing);
            }
        }

        return PackingSubmissionResponseDTO.builder()
                .packingId(packingId)
                .lineItems(lineItemResponses)
                .build();
    }

    @Override
    public List<PackingSubmission> getAllPackingSubmissions() {
        return packingSubmissionRepository.findAll();
    }

    @Override
    @Transactional
    public void deleteAllPackingSubmissions() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║ 🗑️  DELETE ALL PACKING SUBMISSIONS    ║");
        System.out.println("╚════════════════════════════════════════╝\n");

        try {
            long totalCount = packingSubmissionRepository.count();
            System.out.println("📊 Total packing submissions before deletion: " + totalCount);

            packingSubmissionRepository.deleteAll();

            long afterCount = packingSubmissionRepository.count();
            System.out.println("✅ All packing submissions deleted successfully!");
            System.out.println("📊 Total packing submissions after deletion: " + afterCount);
            System.out.println("\n╔════════════════════════════════════════╗");
            System.out.println("║     ✅ DELETION COMPLETE               ║");
            System.out.println("╚════════════════════════════════════════╝\n");
        } catch (Exception e) {
            System.err.println("❌ Error deleting all packing submissions: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to delete all packing submissions: " + e.getMessage());
        }
    }

    @Override
    public void updatePackingPdf(String packingId, String pdf) {

        List<PackingSubmission> submissions = packingSubmissionRepository.findByPackingId(packingId);

        if (submissions.isEmpty()) {
            throw new RuntimeException("Packing records not found for packingId: " + packingId);
        }

        for (PackingSubmission submission : submissions) {
            submission.setPdf(pdf);
        }

        packingSubmissionRepository.saveAll(submissions);
    }

    private String generatePackingId() {

        String prefix = "MEPA";

        LocalDate today = LocalDate.now();
        String year = String.format("%02d", today.getYear() % 100);
        String month = String.format("%02d", today.getMonthValue());

        String base = prefix + year + month;

        String lastPackingId = packingSubmissionRepository.findLastPackingId();

        int nextSeries = 1;

        if (lastPackingId != null && lastPackingId.startsWith(base)) {
            String lastSeries = lastPackingId.substring(8);
            nextSeries = Integer.parseInt(lastSeries) + 1;
        }

        return base + String.format("%04d", nextSeries);
    }
}
