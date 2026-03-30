package com.indona.invento.services.impl;

import com.indona.invento.dao.PackingSchedulerRepository;
import com.indona.invento.dao.SalesOrderRepository;
import com.indona.invento.dto.PackingInstructionDTO;
import com.indona.invento.dto.PackingScheduleDetailsDTO;
import com.indona.invento.entities.PackingEntityScheduler;

import com.indona.invento.entities.PackingInstruction;
import com.indona.invento.entities.SalesOrder;
import com.indona.invento.services.PackingSchedulerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PackingSchedulerServiceImpl implements PackingSchedulerService {

    @Autowired
    private PackingSchedulerRepository packingSchedulerRepository;

    @Autowired
    private SalesOrderRepository salesOrderRepository;

    @Override
    public List<PackingEntityScheduler> getAllPackingSchedules() {
        return packingSchedulerRepository.findAll();
    }


    @Override
    public PackingInstructionDTO getPackingInstructionBySoNumber(String soNumber) {
        SalesOrder order = salesOrderRepository.findBySoNumber(soNumber).orElse(null);
        if (order == null || order.getPackingInstruction() == null) {
            return null;
        }

        PackingInstruction instruction = order.getPackingInstruction();

        return PackingInstructionDTO.builder()
                .typeOfPacking(instruction.getTypeOfPacking())
                .weightInstructions(instruction.getWeightInstructions())
                .additionalRemarks(instruction.getAdditionalRemarks())
                .build();
    }

    @Override
    public PackingScheduleDetailsDTO getDetailsBySoNumberAndLineNumber(String soNumber, String lineNumber) {
        System.out.println("\n══════════════════════════════════════════════════════════════════");
        System.out.println("📦 [PackingScheduler] GET DETAILS BY SO NUMBER AND LINE NUMBER");
        System.out.println("══════════════════════════════════════════════════════════════════");
        System.out.println("   SO Number: " + soNumber);
        System.out.println("   Line Number: " + lineNumber);

        PackingEntityScheduler packing = packingSchedulerRepository.findBySoNumberAndLineNumber(soNumber, lineNumber);

        if (packing == null) {
            System.out.println("   ❌ No packing schedule found for SO: " + soNumber + ", Line: " + lineNumber);
            return null;
        }

        System.out.println("   ✅ Found packing schedule - ID: " + packing.getId());
        System.out.println("   Customer Code: " + packing.getCustomerCode());
        System.out.println("   Customer Name: " + packing.getCustomerName());
        System.out.println("   Order Type: " + packing.getOrderType());
        System.out.println("   Product Category: " + packing.getProductCategory());
        System.out.println("   Brand: " + packing.getBrand());
        System.out.println("   Grade: " + packing.getGrade());
        System.out.println("   Temper: " + packing.getTemper());
        System.out.println("   Dimension: " + packing.getDimension());
        System.out.println("══════════════════════════════════════════════════════════════════\n");

        return PackingScheduleDetailsDTO.builder()
                .customerCode(packing.getCustomerCode())
                .customerName(packing.getCustomerName())
                .orderType(packing.getOrderType())
                .productCategory(packing.getProductCategory())
                .brand(packing.getBrand())
                .grade(packing.getGrade())
                .temper(packing.getTemper())
                .dimension(packing.getDimension())
                .itemDescription(packing.getItemDescription())
                .build();
    }

    @Override
    public void deleteAllPackingSchedules() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║  🗑️  DELETE ALL PACKING SCHEDULES     ║");
        System.out.println("╚════════════════════════════════════════╝\n");

        try {
            long totalCount = packingSchedulerRepository.count();
            System.out.println("📊 Total packing schedules before deletion: " + totalCount);

            packingSchedulerRepository.deleteAll();

            long afterCount = packingSchedulerRepository.count();
            System.out.println("✅ All packing schedules deleted successfully!");
            System.out.println("📊 Total packing schedules after deletion: " + afterCount);
            System.out.println("\n╔════════════════════════════════════════╗");
            System.out.println("║     ✅ DELETION COMPLETE               ║");
            System.out.println("╚════════════════════════════════════════╝\n");
        } catch (Exception e) {
            System.err.println("❌ Error deleting all packing schedules: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to delete all packing schedules: " + e.getMessage());
        }
    }
}
