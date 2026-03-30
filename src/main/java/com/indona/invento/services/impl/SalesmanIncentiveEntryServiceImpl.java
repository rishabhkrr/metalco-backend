package com.indona.invento.services.impl;

import com.indona.invento.dao.SalesmanIncentiveEntryRepository;
import com.indona.invento.dto.CustomerOverdueResponseDTO;
import com.indona.invento.dto.SalesmanIncentiveUpdateDTO;
import com.indona.invento.entities.SalesmanIncentiveEntryEntity;
import com.indona.invento.services.SalesmanIncentiveEntryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SalesmanIncentiveEntryServiceImpl implements SalesmanIncentiveEntryService {

    private final SalesmanIncentiveEntryRepository repository;

    @Override
    public List<SalesmanIncentiveEntryEntity> getAllIncentives() {
        return repository.findAll();
    }


    @Override
    public boolean updatePaymentDetails(SalesmanIncentiveUpdateDTO dto) {
        Optional<SalesmanIncentiveEntryEntity> optional = repository.findById(dto.getIncentiveId());
        if (optional.isEmpty()) return false;

        SalesmanIncentiveEntryEntity entry = optional.get();

        // ✅ Set received details
        entry.setAmountReceived(dto.getAmountReceived());
        entry.setDateOfPayment(dto.getDateOfPayment());

        // ✅ Null-safe lapse days calculation
        LocalDate targetDate = entry.getTargetDateOfPayment();
        LocalDate paymentDate = dto.getDateOfPayment();
        int lapseDays = 0;
        if (targetDate != null && paymentDate != null && paymentDate.isAfter(targetDate)) {
            lapseDays = (int) ChronoUnit.DAYS.between(targetDate, paymentDate);
        }
        entry.setNumberOfDaysLapse(lapseDays);

        // ✅ Null-safe lapse interest calculation
        BigDecimal lapseRate = entry.getLapseInterestRate() != null ? entry.getLapseInterestRate() : BigDecimal.ZERO;
        BigDecimal lapseInterestAmount = lapseRate.multiply(BigDecimal.valueOf(lapseDays));
        entry.setLapseInterestAmount(lapseInterestAmount);

        // ✅ Final incentive amount
        BigDecimal incentiveAmount = entry.getIncentiveAmount() != null ? entry.getIncentiveAmount() : BigDecimal.ZERO;
        BigDecimal finalIncentiveAmount = incentiveAmount.subtract(lapseInterestAmount);
        entry.setFinalIncentiveAmount(finalIncentiveAmount);

        // ✅ Payment status based on totalAmount
        BigDecimal received = dto.getAmountReceived() != null ? dto.getAmountReceived() : BigDecimal.ZERO;
        BigDecimal totalAmount = entry.getTotalAmount() != null ? entry.getTotalAmount() : BigDecimal.ZERO;

        if (received.compareTo(BigDecimal.ZERO) == 0) {
            entry.setPaymentStatus("Pending");
        } else if (received.compareTo(totalAmount) < 0) {
            entry.setPaymentStatus("Partially Received");
        } else {
            entry.setPaymentStatus("Received");
        }

        repository.save(entry);
        return true;
    }

    @Override
    public CustomerOverdueResponseDTO getCustomerOverdueDetails(String customerName, String customerCode) {
        // Get all pending entries for the customer
        List<SalesmanIncentiveEntryEntity> pendingEntries = repository
                .findByCustomerCodeAndCustomerNameAndPaymentStatus(customerCode, customerName, "Pending");

        // Get the last entry for bill date and due date
        List<SalesmanIncentiveEntryEntity> allEntries = repository
                .findByCustomerCodeAndCustomerNameOrderByDispatchDateDesc(customerCode, customerName);

        if (allEntries.isEmpty()) {
            return CustomerOverdueResponseDTO.builder()
                    .creditDays(0)
                    .billDate(null)
                    .dueDate(null)
                    .dueAmount(BigDecimal.ZERO)
                    .build();
        }

        // ⭐ Step 1: Filter pending entries where dueAmount > 0
        List<SalesmanIncentiveEntryEntity> validPendingEntries = pendingEntries.stream()
                .filter(e -> {
                    BigDecimal total = e.getTotalAmount() != null ? e.getTotalAmount() : BigDecimal.ZERO;
                    BigDecimal received = e.getAmountReceived() != null ? e.getAmountReceived() : BigDecimal.ZERO;
                    BigDecimal due = total.subtract(received);
                    return due.compareTo(BigDecimal.ZERO) > 0; // only unpaid entries
                })
                .toList();

        // ⭐ Step 2: Pick earliest due date from valid entries
        Optional<SalesmanIncentiveEntryEntity> earliestPendingOpt = validPendingEntries.stream()
                .filter(e -> e.getTargetDateOfPayment() != null)
                .min((a, b) -> a.getTargetDateOfPayment().compareTo(b.getTargetDateOfPayment()));

        SalesmanIncentiveEntryEntity referenceEntry;

        if (earliestPendingOpt.isPresent()) {
            // Earliest valid unpaid entry
            referenceEntry = earliestPendingOpt.get();
        } else {
            // ⭐ All due amounts are zero ⇒ fallback to latest entry
            referenceEntry = allEntries.get(0);
        }

        LocalDate earliestDueDate = referenceEntry.getTargetDateOfPayment();

        // ⭐ Step 3: Calculate total due amount (all unpaid)
        BigDecimal totalDueAmount = pendingEntries.stream()
                .map(entry -> {
                    BigDecimal total = entry.getTotalAmount() != null ? entry.getTotalAmount() : BigDecimal.ZERO;
                    BigDecimal received = entry.getAmountReceived() != null ? entry.getAmountReceived() : BigDecimal.ZERO;
                    return total.subtract(received);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CustomerOverdueResponseDTO.builder()
                .creditDays(referenceEntry.getCreditDays())       // based on earliest valid entry
                .billDate(referenceEntry.getDispatchDate())       // bill date from the earliest unpaid entry
                .dueDate(earliestDueDate)                         // earliest valid due date
                .dueAmount(totalDueAmount)
                .build();
    }

    @Override
    public boolean updateQuotationStatus(String quotationNo, String status, boolean hasOverdue) {
        // This method would update the quotation status in NewSalesOrder table
        // For now, returning true to indicate success
        // In future, integrate with NewSalesOrderRepository to update the status
        System.out.println("🔄 [updateQuotationStatus] Updating quotation: " + quotationNo);
        System.out.println("   Status: " + status);
        System.out.println("   Has Overdue: " + hasOverdue);
        return true;
    }
}

