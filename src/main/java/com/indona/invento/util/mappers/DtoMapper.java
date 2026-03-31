package com.indona.invento.util.mappers;

@FunctionalInterface
public interface DtoMapper<D, E> {
    E mapDtoToEntity(D dto);
}
