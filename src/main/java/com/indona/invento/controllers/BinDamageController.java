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

import com.indona.invento.entities.BinDamageEntity;
import com.indona.invento.services.BinDamageService;

@RestController
@RequestMapping("/combinations")
public class BinDamageController {

	@Autowired
	private BinDamageService service;

	@GetMapping
	public List<BinDamageEntity> getAllCombinations() {
		return service.getAllCombinations();
	}

	@GetMapping("/{id}")
	public ResponseEntity<BinDamageEntity> getCombinationById(@PathVariable Long id) {
		BinDamageEntity combination = service.getCombinationById(id);
		return combination != null ? new ResponseEntity<>(combination, HttpStatus.OK)
				: new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	@PostMapping
	public ResponseEntity<BinDamageEntity> createCombination(@RequestBody BinDamageEntity combination) {
		BinDamageEntity createdCombination = service.createCombination(combination);
		return new ResponseEntity<>(createdCombination, HttpStatus.CREATED);
	}

	@PutMapping("/{id}")
	public ResponseEntity<BinDamageEntity> updateCombination(@PathVariable Long id,
			@RequestBody BinDamageEntity combination) {
		BinDamageEntity updatedCombination = service.updateCombination(id, combination);
		return updatedCombination != null ? new ResponseEntity<>(updatedCombination, HttpStatus.OK)
				: new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteCombination(@PathVariable Long id) {
		service.deleteCombination(id);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
}
