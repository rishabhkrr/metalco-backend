package com.indona.invento.util.mappers;

@FunctionalInterface
public interface EntityMapper<E, D> {
    D mapEntityToDto(E entity);
}
