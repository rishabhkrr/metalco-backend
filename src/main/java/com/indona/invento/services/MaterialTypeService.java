package com.indona.invento.services;

import com.indona.invento.dto.MaterialTypeDto;
import com.indona.invento.entities.MaterialTypeEntity;

import java.util.List;

public interface MaterialTypeService {
    MaterialTypeEntity addMaterialType(MaterialTypeDto dto);
    List<MaterialTypeDto> getAllMaterialTypes();
}
