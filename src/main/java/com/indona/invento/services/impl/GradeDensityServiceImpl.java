package com.indona.invento.services.impl;

import com.indona.invento.dao.GradeDensityRepository;
import com.indona.invento.dto.GradeDensityRequest;
import com.indona.invento.entities.GradeDensityEntity;
import com.indona.invento.services.GradeDensityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GradeDensityServiceImpl implements GradeDensityService {

    private final GradeDensityRepository repository;

    @Override
    public GradeDensityEntity saveGradeDensity(GradeDensityRequest request) {
        GradeDensityEntity entity = new GradeDensityEntity();
        entity.setGrade(String.valueOf(request.getGrade()));
        entity.setDensity(request.getDensity());
        return repository.save(entity);
    }

    @Override
    public List<GradeDensityEntity> getAll() {
        return repository.findAll();
    }

    @Override
    public Optional<BigDecimal> getDensityByGrade(String grade) {
        return repository.findByGrade(grade)
                .map(GradeDensityEntity::getDensity);
    }
}
