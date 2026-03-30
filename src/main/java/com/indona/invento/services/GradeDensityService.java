package com.indona.invento.services;

import com.indona.invento.dto.GradeDensityRequest;
import com.indona.invento.entities.GradeDensityEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface GradeDensityService {
    GradeDensityEntity saveGradeDensity(GradeDensityRequest request);
    List<GradeDensityEntity> getAll();
    Optional<BigDecimal> getDensityByGrade(String grade);
}
