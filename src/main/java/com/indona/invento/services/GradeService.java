package com.indona.invento.services;

import com.indona.invento.dto.GradeDto;
import com.indona.invento.entities.GradeEntity;

import java.util.List;

public interface GradeService {
    GradeEntity addGrade(GradeDto dto);
    List<GradeDto> getAllGrades();
}
