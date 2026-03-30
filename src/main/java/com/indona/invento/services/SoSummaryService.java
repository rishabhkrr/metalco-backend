package com.indona.invento.services;

import com.indona.invento.dto.JobworkMergedDTO;
import com.indona.invento.dto.SoSummaryDTO;
import com.indona.invento.entities.SoSummaryEntity;
import com.indona.invento.entities.SoSummaryItemEntity;

import java.util.List;

public interface SoSummaryService {
	SoSummaryEntity saveSummary(SoSummaryDTO dto);

	List<SoSummaryEntity> getAllSummaries();

	int updateLrNumber(String soNumber, String lineNumber, String lrNumberUpdation);

	List<JobworkMergedDTO> getJobworkMergedData();


    void deleteAllSummaries();
}
