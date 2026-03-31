package com.indona.invento.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.indona.invento.entities.BinsEntity;
import com.indona.invento.services.BinsService;

@RestController
@RequestMapping("/bins")
public class BinsController {

	@Autowired
	private BinsService binService;

	@GetMapping
	public List<BinsEntity> getAllBins() {
		return binService.getAllBins();
	}

	@GetMapping("/{id}")
	public ResponseEntity<BinsEntity> getBinById(@PathVariable Long id) {
		BinsEntity customer = binService.getBinById(id);
		return customer != null ? new ResponseEntity<>(customer, HttpStatus.OK)
				: new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	@PostMapping
	public ResponseEntity<BinsEntity> createBin(@RequestBody BinsEntity customer) {
		BinsEntity createdBin = binService.createBin(customer);
		return new ResponseEntity<>(createdBin, HttpStatus.CREATED);
	}

	@PutMapping("/{id}")
	public ResponseEntity<BinsEntity> updateBin(@PathVariable Long id, @RequestBody BinsEntity customer) {
		BinsEntity updatedBin = binService.updateBin(id, customer);
		return updatedBin != null ? new ResponseEntity<>(updatedBin, HttpStatus.OK)
				: new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteBin(@PathVariable Long id) {
		binService.deleteBin(id);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
}