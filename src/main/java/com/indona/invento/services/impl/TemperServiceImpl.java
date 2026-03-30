package com.indona.invento.services.impl;

import com.indona.invento.dao.TemperRepository;
import com.indona.invento.dto.TemperDto;
import com.indona.invento.entities.TemperEntity;
import com.indona.invento.services.TemperService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TemperServiceImpl implements TemperService {

    private final TemperRepository temperRepository;

    @Override
    public TemperEntity addTemper(TemperDto dto) {
        if (temperRepository.existsByTemperValue(dto.getTemperValue())) {
            throw new RuntimeException("Temper already exists: " + dto.getTemperValue());
        }

        TemperEntity entity = TemperEntity.builder()
                .temperValue(dto.getTemperValue())
                .build();

        return temperRepository.save(entity);
    }

    @Override
    public List<TemperDto> getAllTempers() {
        return temperRepository.findAll()
                .stream()
                .map(t -> {
                    TemperDto dto = new TemperDto();
                    dto.setTemperValue(t.getTemperValue());
                    return dto;
                })
                .collect(Collectors.toList());
    }


}
