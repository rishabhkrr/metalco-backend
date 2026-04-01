package com.indona.invento.services.impl;

import com.indona.invento.dao.GateEntryPackingAndDispatchRepository;
import com.indona.invento.dto.GateEntryEditDTO;
import com.indona.invento.dto.GateEntryPackingAndDispatchRequestDTO;
import com.indona.invento.dto.GateEntryUpdateDTO;
import com.indona.invento.entities.GateEntryPackingAndDispatch;
import com.indona.invento.services.AuditLogService;
import com.indona.invento.services.GateEntryPackingAndDispatchService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GateEntryPackingAndDispatchServiceImpl implements GateEntryPackingAndDispatchService {

	@Autowired
	private GateEntryPackingAndDispatchRepository repository;

	@Autowired
	private AuditLogService auditLogService;

	private String generateGateEntryRefNo() {
		String prefix = "MEGE";
		String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMM"));
		String uniquePart = java.util.UUID.randomUUID().toString().substring(0, 6).toUpperCase();
		return prefix + datePart + uniquePart;
	}

	@Override
	public GateEntryPackingAndDispatch saveGateEntry(GateEntryPackingAndDispatchRequestDTO dto) {

		GateEntryPackingAndDispatch entry = GateEntryPackingAndDispatch.builder()
				.unitPackingAndDispatch(dto.getUnitPackingAndDispatch())
				.purposePackingAndDispatch(dto.getPurposePackingAndDispatch())
				.modePackingAndDispatch(dto.getModePackingAndDispatch())
				.medciScan(dto.getMedciScan())
				.invoiceNumberPackingAndDispatch(dto.getInvoiceNumberPackingAndDispatch())
				.medcNumberPackingAndDispatch(dto.getMedcNumberPackingAndDispatch())
				.medciNumberPackingAndDispatch(dto.getMedciNumberPackingAndDispatch())
				.ewayBillNumberPackingAndDispatch(dto.getEwayBillNumberPackingAndDispatch())
				.vehicleNumberPackingAndDispatch(dto.getVehicleNumberPackingAndDispatch())
				.driverNamePackingAndDispatch(dto.getDriverNamePackingAndDispatch())
				.medcScan(dto.getMedcScan())
				.medcpScan(dto.getMedcpScan())
				.medcpNumberPackingAndDispatch(dto.getMedcpNumberPackingAndDispatch())
				.invoiceScanPackingAndDispatch(dto.getInvoiceScanPackingAndDispatch())
				.vehicleDocumentsScanPackingAndDispatch(dto.getVehicleDocumentsScanPackingAndDispatch())
				.vehicleWeighmentStatusPackingAndDispatch("PENDING")
				.vehicleOutStatusPackingAndDispatch("IN")
				.gateEntryRefNoPackingAndDispatch(generateGateEntryRefNo())
				.timeStampPackingAndDispatch(LocalDateTime.now()).build();

		return repository.save(entry);
	}

	@Override
	public List<GateEntryPackingAndDispatch> getAllGateEntries() {
		return repository.findAll();
	}

	@Override
	public List<String> getVehicleNumbersWithInStatus() {
		//  Fetch only those vehicles whose status = "IN"
		return repository.findByVehicleOutStatusPackingAndDispatchIgnoreCase("IN").stream()
				.map(GateEntryPackingAndDispatch::getVehicleNumberPackingAndDispatch).distinct()
				.collect(Collectors.toList());
	}

	@Override
	public List<String> getVehicleNumbersFiltered(String purpose, String mode) {
		// Fetch vehicles where Status=IN, Purpose matches, Mode matches
		return repository.findByVehicleOutStatusPackingAndDispatchIgnoreCase("IN").stream()
				.filter(entry -> purpose.equalsIgnoreCase(entry.getPurposePackingAndDispatch()))
				.filter(entry -> mode.equalsIgnoreCase(entry.getModePackingAndDispatch()))
				.map(GateEntryPackingAndDispatch::getVehicleNumberPackingAndDispatch)
				.distinct()
				.collect(Collectors.toList());
	}

	@Override
	@Transactional
	public GateEntryPackingAndDispatch updateGateEntryByRefNo(String refNo, GateEntryUpdateDTO dto) {
		GateEntryPackingAndDispatch existing = repository.findByGateEntryRefNoPackingAndDispatch(refNo)
				.orElseThrow(() -> new RuntimeException("Gate Entry not found with Ref No: " + refNo));

		// ✅ Update only required fields
		if (dto.getInvoiceNumberPackingAndDispatch() != null) {
			existing.setInvoiceNumberPackingAndDispatch(dto.getInvoiceNumberPackingAndDispatch());
		}

		if (dto.getMedcNumberPackingAndDispatch() != null) {
			existing.setMedcNumberPackingAndDispatch(dto.getMedcNumberPackingAndDispatch());
		}

		if (dto.getMedciNumberPackingAndDispatch() != null) {
			existing.setMedciNumberPackingAndDispatch(dto.getMedciNumberPackingAndDispatch());
		}

		if (dto.getEwayBillNumberPackingAndDispatch() != null) {
			existing.setEwayBillNumberPackingAndDispatch(dto.getEwayBillNumberPackingAndDispatch());
		}

		return repository.save(existing);
	}

	/**
	 * FRD Gate OUT Validation for Dispatch:
	 * Vehicle can ONLY exit when weighment is completed.
	 */
	@Override
	@Transactional
	public GateEntryPackingAndDispatch markVehicleOut(String refNo) {
		GateEntryPackingAndDispatch entry = repository.findByGateEntryRefNoPackingAndDispatch(refNo)
				.orElseThrow(() -> new RuntimeException("Gate Entry not found with Ref No: " + refNo));

		// FRD Validation: Vehicle weighment must be completed
		String weighmentStatus = entry.getVehicleWeighmentStatusPackingAndDispatch();
		if (weighmentStatus == null || !"COMPLETED".equalsIgnoreCase(weighmentStatus)) {
			throw new RuntimeException(
					"Gate OUT blocked: Vehicle weighment is not completed. " +
					"Current status: " + (weighmentStatus != null ? weighmentStatus : "PENDING") +
					". Dispatch vehicle must be weighed before gate exit.");
		}

		entry.setVehicleOutStatusPackingAndDispatch("OUT");
		GateEntryPackingAndDispatch saved = repository.save(entry);

		// Audit log
		auditLogService.logAction("STATUS_CHANGE", "GATE_DISPATCH", "GateEntryPackingAndDispatch",
				entry.getId(), entry.getGateEntryRefNoPackingAndDispatch(), "IN", "OUT",
				"Dispatch vehicle " + entry.getVehicleNumberPackingAndDispatch() + " marked OUT. " +
				"Ref: " + entry.getGateEntryRefNoPackingAndDispatch(),
				"SECURITY", entry.getUnitPackingAndDispatch());

		return saved;
	}

	@Override
	public GateEntryPackingAndDispatch getGateEntryByRefNo(String refNo) {
		return repository.findByGateEntryRefNoPackingAndDispatch(refNo)
				.orElseThrow(() -> new RuntimeException("Gate Entry not found with Ref No: " + refNo));
	}

	@Override
	@Transactional
	public GateEntryPackingAndDispatch editGateEntryByRefNo(String refNo, GateEntryEditDTO dto) {
		GateEntryPackingAndDispatch entry = repository.findByGateEntryRefNoPackingAndDispatch(refNo)
				.orElseThrow(() -> new RuntimeException("Gate Entry not found with Ref No: " + refNo));

		if (dto.getUnitPackingAndDispatch() != null)
			entry.setUnitPackingAndDispatch(dto.getUnitPackingAndDispatch());

		if (dto.getPurposePackingAndDispatch() != null)
			entry.setPurposePackingAndDispatch(dto.getPurposePackingAndDispatch());

		if (dto.getModePackingAndDispatch() != null)
			entry.setModePackingAndDispatch(dto.getModePackingAndDispatch());

		if (dto.getInvoiceNumberPackingAndDispatch() != null)
			entry.setInvoiceNumberPackingAndDispatch(dto.getInvoiceNumberPackingAndDispatch());

		if( dto.getMedcScan() != null)
			entry.setMedcScan( dto.getMedcScan());

		if( dto.getMedcpScan() != null)
			entry.setMedcpScan( dto.getMedcpScan());

		if (dto.getMedcNumberPackingAndDispatch() != null)
			entry.setMedcNumberPackingAndDispatch(dto.getMedcNumberPackingAndDispatch());

		if (dto.getMedciNumberPackingAndDispatch() != null)
			entry.setMedciNumberPackingAndDispatch(dto.getMedciNumberPackingAndDispatch());

		if (dto.getEwayBillNumberPackingAndDispatch() != null)
			entry.setEwayBillNumberPackingAndDispatch(dto.getEwayBillNumberPackingAndDispatch());

		if (dto.getVehicleNumberPackingAndDispatch() != null)
			entry.setVehicleNumberPackingAndDispatch(dto.getVehicleNumberPackingAndDispatch());

		if (dto.getDriverNamePackingAndDispatch() != null)
			entry.setDriverNamePackingAndDispatch(dto.getDriverNamePackingAndDispatch());

		if( dto.getMedcpNumberPackingAndDispatch() != null)
			entry.setMedcpNumberPackingAndDispatch(dto.getMedcpNumberPackingAndDispatch());

		if (dto.getInvoiceScanPackingAndDispatch() != null)
			entry.setInvoiceScanPackingAndDispatch(dto.getInvoiceScanPackingAndDispatch());

		if (dto.getVehicleDocumentsScanPackingAndDispatch() != null)
			entry.setVehicleDocumentsScanPackingAndDispatch(dto.getVehicleDocumentsScanPackingAndDispatch());

		return repository.save(entry);
	}

	@Override
	public void deleteAllGateEntries() {
		System.out.println("\n╔════════════════════════════════════════╗");
		System.out.println("║  🗑️  DELETE ALL GATE ENTRIES          ║");
		System.out.println("╚════════════════════════════════════════╝\n");

		try {
			long totalCount = repository.count();
			System.out.println("📊 Total gate entries before deletion: " + totalCount);

			repository.deleteAll();

			long afterCount = repository.count();
			System.out.println("✅ All gate entries deleted successfully!");
			System.out.println("📊 Total gate entries after deletion: " + afterCount);
			System.out.println("\n╔════════════════════════════════════════╗");
			System.out.println("║     ✅ DELETION COMPLETE               ║");
			System.out.println("╚════════════════════════════════════════╝\n");
		} catch (Exception e) {
			System.err.println("❌ Error deleting all gate entries: " + e.getMessage());
			e.printStackTrace();
			throw new RuntimeException("Failed to delete all gate entries: " + e.getMessage());
		}
	}

}
