package com.indona.invento.services.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.indona.invento.entities.BinDamageEntity;
import com.indona.invento.dao.BinDamageRepository;
import com.indona.invento.services.BinDamageService;

@Service
public class BinDamageServiceImpl implements BinDamageService {

	@Autowired
	private BinDamageRepository repository;

	@Override
	public List<BinDamageEntity> getAllCombinations() {
		return repository.findAll();
	}

	@Override
	public BinDamageEntity getCombinationById(Long id) {
		Optional<BinDamageEntity> combination = repository.findById(id);
		return combination.orElse(null);
	}

	@Override
	public BinDamageEntity createCombination(BinDamageEntity combination) {
		return repository.save(combination);
	}

	@Override
	public BinDamageEntity updateCombination(Long id, BinDamageEntity combination) {
		Optional<BinDamageEntity> existingCombination = repository.findById(id);
		if (existingCombination.isPresent()) {
			BinDamageEntity updatedCombination = existingCombination.get();
			updatedCombination.setBinName(combination.getBinName());
			updatedCombination.setPartNumber(combination.getPartNumber());
			updatedCombination.setQuantity(combination.getQuantity()); // Update quantity
			return repository.save(updatedCombination);
		} else {
			return null;
		}
	}

	@Override
	public void deleteCombination(Long id) {
		repository.deleteById(id);
	}
}
