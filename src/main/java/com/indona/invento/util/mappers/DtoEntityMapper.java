package com.indona.invento.util.mappers;

import java.util.List;
import java.util.stream.Collectors;

public class DtoEntityMapper<D, E> {

    public List<E> mapDtoListToEntityList(List<D> dtoList, DtoMapper<D, E> mapper) {
        return dtoList.stream()
                .map(mapper::mapDtoToEntity)
                .collect(Collectors.toList());
    }

    public E mapDtoToEntity(D dto, DtoMapper<D, E> mapper) {
        return mapper.mapDtoToEntity(dto);
    }
}
