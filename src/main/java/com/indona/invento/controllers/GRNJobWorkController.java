package com.indona.invento.controllers;

import com.indona.invento.entities.GRNJobWorkEntity;
import com.indona.invento.services.GRNJobWorkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/grn-jobwork")
@RequiredArgsConstructor
public class GRNJobWorkController {

	private final GRNJobWorkService service;

	@PostMapping("/bulk-save")
	public ResponseEntity<List<GRNJobWorkEntity>> saveAll(@RequestBody List<GRNJobWorkEntity> entities) {
		return ResponseEntity.ok(service.saveAll(entities));
	}

	@GetMapping("/all")
	public ResponseEntity<List<GRNJobWorkEntity>> getAll() {
		return ResponseEntity.ok(service.getAll());
	}

	@PutMapping("/{id}")
	public ResponseEntity<GRNJobWorkEntity> updateById(@PathVariable Long id, @RequestBody GRNJobWorkEntity entity) {
		return ResponseEntity.ok(service.updateById(id, entity));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteById(@PathVariable Long id) {
		service.deleteById(id);
		return ResponseEntity.noContent().build();
	}
	
	@GetMapping("/pending-invoices")
	public ResponseEntity<List<String>> getPendingInvoiceNumbers() {
	    return ResponseEntity.ok(service.getPendingInvoiceNumbers());
	}
	
	@PatchMapping("/material-unloading/{invoiceNumber}")
	public ResponseEntity<List<GRNJobWorkEntity>> updateMaterialUnloadingStatus(
	        @PathVariable String invoiceNumber,
	        @RequestBody String materialUnloadingStatus
	) {
	    return ResponseEntity.ok(
	            service.updateMaterialUnloadingStatus(invoiceNumber, materialUnloadingStatus)
	    );
	}

	// FRD §18.7: Approval workflow
	@PutMapping("/approve/{id}")
	public ResponseEntity<GRNJobWorkEntity> approveById(@PathVariable Long id) {
	    return ResponseEntity.ok(service.approveById(id));
	}

	@PutMapping("/reject/{id}")
	public ResponseEntity<GRNJobWorkEntity> rejectById(@PathVariable Long id) {
	    return ResponseEntity.ok(service.rejectById(id));
	}

}
