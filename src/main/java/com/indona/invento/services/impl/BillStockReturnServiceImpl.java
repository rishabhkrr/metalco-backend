package com.indona.invento.services.impl;

import com.indona.invento.dao.BillStockReturnRepository;
import com.indona.invento.dao.StockSummaryRepository;
import com.indona.invento.dto.BillStockReturnDTO;
import com.indona.invento.entities.BillStockReturnEntity;
import com.indona.invento.entities.StockSummaryEntity;
import com.indona.invento.services.BillStockReturnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class BillStockReturnServiceImpl implements BillStockReturnService {

    @Autowired
    private BillStockReturnRepository repository;

    @Autowired
    private StockSummaryRepository stockSummaryRepository;

    @Override
    @Transactional
    public BillStockReturnEntity createStockReturn(BillStockReturnDTO dto) {

        // Step 1: Generate sales return number
        String salesReturnNumber = generateSalesReturnNumber();

        // Step 2: Determine storage location based on stock selection
        String store = "Warehouse";
        String storageArea = "REJECTION".equalsIgnoreCase(dto.getStockSelection()) ? "Rejection" : "Common Area";
        String rackColumnBinNumber = "Common Bin";

        // Step 3: Create stock return entity
        BillStockReturnEntity entity = BillStockReturnEntity.builder()
                .timestamp(LocalDateTime.now())
                .salesReturnNumber(salesReturnNumber)
                .invoiceNumber(dto.getInvoiceNumber())
                .soNumber(dto.getSoNumber())
                .lineNumber(dto.getLineNumber())
                .unit(dto.getUnit())
                .customerCode(dto.getCustomerCode())
                .customerName(dto.getCustomerName())
                .customerBillingAddress(dto.getCustomerBillingAddress())
                .customerShippingAddress(dto.getCustomerShippingAddress())
                .orderType(dto.getOrderType())
                .productCategory(dto.getProductCategory())
                .itemDescription(dto.getItemDescription())
                .brand(dto.getBrand())
                .grade(dto.getGrade())
                .temper(dto.getTemper())
                .dimension(dto.getDimension())
                .uomKg(dto.getUomKg())
                .uomNo(dto.getUomNo())
                .itemPrice(dto.getItemPrice())
                .returnQuantityKg(dto.getReturnQuantityKg())
                .returnQuantityNo(dto.getReturnQuantityNo())
                .amount(dto.getAmount())
                .cgst(dto.getCgst())
                .sgst(dto.getSgst())
                .igst(dto.getIgst())
                .transportationCharges(dto.getTransportationCharges())
                .totalAmount(dto.getTotalAmount())
                .stockSelection(dto.getStockSelection())
                .createdBy(dto.getCreatedBy())
                .build();

        BillStockReturnEntity savedReturn = repository.save(entity);

        // Step 4: Create NEW stock entry in StockSummary
        Integer returnQtyNo = 0;
        if (dto.getReturnQuantityNo() != null) {
            returnQtyNo = dto.getReturnQuantityNo().intValue();
        }

        StockSummaryEntity newStockEntry = StockSummaryEntity.builder()
                .unit(dto.getUnit())
                .store(store)
                .storageArea(storageArea)
                .rackColumnShelfNumber(rackColumnBinNumber)
                .productCategory(dto.getProductCategory())
                .itemDescription(dto.getItemDescription())
                .brand(dto.getBrand())
                .grade(dto.getGrade())
                .temper(dto.getTemper())
                .dimension(dto.getDimension())
                .quantityKg(dto.getReturnQuantityKg() != null ? dto.getReturnQuantityKg() : BigDecimal.ZERO)
                .quantityNo(returnQtyNo)
                .itemPrice(dto.getItemPrice())
                .materialType(null) // Not provided in return
                .reprintQr(false)
                .sectionNo(null)
                .pickListLocked(false)
                .build();

        stockSummaryRepository.save(newStockEntry);

        // Step 5: Update total quantity in ALL existing stock entries (match by unit + itemDescription)
        List<StockSummaryEntity> existingStocks = stockSummaryRepository
                .findByUnitAndItemDescription(dto.getUnit(), dto.getItemDescription());

        if (!existingStocks.isEmpty()) {
            BigDecimal returnKg = dto.getReturnQuantityKg() != null ? dto.getReturnQuantityKg() : BigDecimal.ZERO;

            // Update ALL matching stock entries
            for (StockSummaryEntity existingStock : existingStocks) {
                // Add returned quantities to existing totals
                BigDecimal currentKg = existingStock.getQuantityKg() != null ? existingStock.getQuantityKg() : BigDecimal.ZERO;
                Integer currentNo = existingStock.getQuantityNo() != null ? existingStock.getQuantityNo() : 0;

                existingStock.setQuantityKg(currentKg.add(returnKg));
                existingStock.setQuantityNo(currentNo + returnQtyNo);

                stockSummaryRepository.save(existingStock);
            }
        }
        // If no existing stock found, the new entry created above serves as the total

        return savedReturn;
    }

    /**
     * Generate sales return number in format: MECRD(YY)(MM)(0001)
     * Example: MECRD250101, MECRD250102, MECRD251201
     */
    private String generateSalesReturnNumber() {
        LocalDateTime now = LocalDateTime.now();
        String yearMonth = now.format(DateTimeFormatter.ofPattern("yyMM"));

        // Get the latest sales return number
        Optional<String> latestNumberOpt = repository.findLatestSalesReturnNumber();

        int nextSequence = 1;

        if (latestNumberOpt.isPresent()) {
            String latestNumber = latestNumberOpt.get();
            // Extract year-month and sequence from latest number
            // Format: MECRD + YYMM + NNNN
            if (latestNumber != null && latestNumber.length() >= 13) {
                String latestYearMonth = latestNumber.substring(5, 9); // Extract YYMM
                String latestSequence = latestNumber.substring(9); // Extract sequence

                if (yearMonth.equals(latestYearMonth)) {
                    // Same month, increment sequence
                    try {
                        nextSequence = Integer.parseInt(latestSequence) + 1;
                    } catch (NumberFormatException e) {
                        nextSequence = 1;
                    }
                }
                // Different month, start from 1
            }
        }

        // Format: MECRD + YYMM + 0001
        return String.format("MECRD%s%04d", yearMonth, nextSequence);
    }

    @Override
    public List<BillStockReturnEntity> getAllStockReturns() {
        return repository.findAll();
    }

    @Override
    public List<BillStockReturnEntity> getStockReturnsByInvoice(String invoiceNumber) {
        return repository.findByInvoiceNumber(invoiceNumber);
    }

    @Override
    public List<BillStockReturnEntity> getStockReturnsBySo(String soNumber) {
        return repository.findBySoNumber(soNumber);
    }

    @Override
    public List<BillStockReturnEntity> getStockReturnsByType(String stockSelection) {
        return repository.findByStockSelection(stockSelection);
    }

    @Override
    public BillStockReturnEntity getStockReturnById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Stock return not found with id: " + id));
    }

    @Override
    public void deleteAllStockReturns() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║  🗑️  DELETE ALL BILL STOCK RETURNS    ║");
        System.out.println("╚════════════════════════════════════════╝\n");

        try {
            long totalCount = repository.count();
            System.out.println("📊 Total stock returns before deletion: " + totalCount);

            repository.deleteAll();

            long afterCount = repository.count();
            System.out.println("✅ All stock returns deleted successfully!");
            System.out.println("📊 Total stock returns after deletion: " + afterCount);
            System.out.println("\n╔════════════════════════════════════════╗");
            System.out.println("║     ✅ DELETION COMPLETE               ║");
            System.out.println("╚════════════════════════════════════════╝\n");
        } catch (Exception e) {
            System.err.println("❌ Error deleting all stock returns: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to delete all stock returns: " + e.getMessage());
        }
    }
}

