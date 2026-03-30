package com.indona.invento.util.mappers;

import java.util.List;
import java.util.stream.Collectors;

public class EntityDtoMapper<E, D> {

    public List<D> mapEntityListToDtoList(List<E> entityList, EntityMapper<E, D> mapper) {
        return entityList.stream()
                .map(mapper::mapEntityToDto)
                .collect(Collectors.toList());
    }

    public D mapEntityToDto(E entity, EntityMapper<E, D> mapper) {
        return mapper.mapEntityToDto(entity);
    }
}