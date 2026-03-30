package com.indona.invento.services;

import com.indona.invento.dto.PackingListJobWorkDTO;
import com.indona.invento.dto.PackingListJobWorkResponseDTO;
import com.indona.invento.entities.PackingListJobWorkEntity;

import java.util.List;

public interface PackingListJobWorkService {
	List<PackingListJobWorkEntity> savePackingList(List<PackingListJobWorkDTO> dtos);

	List<PackingListJobWorkEntity> getAllPackingLists();

	List<String> getAllPackingListNumbers();

	List<PackingListJobWorkResponseDTO> getByPackingListNumber(String packingListNumber);
}