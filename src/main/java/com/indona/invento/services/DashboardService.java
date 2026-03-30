package com.indona.invento.services;

import com.indona.invento.dto.ChartsPayloadDto;
import com.indona.invento.dto.ChartsResponseDto;

public interface DashboardService {

	ChartsResponseDto getDhuChart(ChartsPayloadDto payload);
	ChartsResponseDto getDefectChart(ChartsPayloadDto payload);
	ChartsResponseDto getAreaChart(ChartsPayloadDto payload);
	ChartsResponseDto getOccChart(ChartsPayloadDto payload);
}
