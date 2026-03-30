package com.indona.invento.services;

import com.indona.invento.dto.TemperDto;
import com.indona.invento.entities.TemperEntity;

import java.util.List;

public interface TemperService {
    TemperEntity addTemper(TemperDto dto);
    List<TemperDto> getAllTempers();
}
