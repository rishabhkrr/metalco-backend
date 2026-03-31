package com.indona.invento.services.impl;

import com.indona.invento.dao.GradeRepository;
import com.indona.invento.dto.GradeDto;
import com.indona.invento.entities.GradeEntity;
import com.indona.invento.services.GradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GradeServiceImpl implements GradeService {

    private final GradeRepository gradeRepository;

    @Override
    public GradeEntity addGrade(GradeDto dto) {
        if (gradeRepository.existsByGradeValue(dto.getGradeValue())) {
            throw new RuntimeException("Grade already exists: " + dto.getGradeValue());
        }

        GradeEntity entity = GradeEntity.builder()
                .gradeValue(dto.getGradeValue())
                .build();

        return gradeRepository.save(entity);
    }

    @Override
    public List<GradeDto> getAllGrades() {
        return gradeRepository.findAll()
                .stream()
                .map(grade -> {
                    GradeDto dto = new GradeDto();
                    dto.setGradeValue(grade.getGradeValue());
                    return dto;
                })
                .collect(Collectors.toList());
    }

}
