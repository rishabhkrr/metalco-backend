package com.indona.invento.services;

import java.util.List;

import com.indona.invento.dto.ExcelRow;
import com.indona.invento.entities.PgmEntity;

public interface PgmService {

	List<PgmEntity> getAllPgms();
	PgmEntity getPgmById(Long id);
	PgmEntity createPgm(PgmEntity supplier);
	PgmEntity updatePgm(Long id, PgmEntity supplier);
    void deletePgm(Long id);
	void processExcelData(List<ExcelRow> rows);
}
