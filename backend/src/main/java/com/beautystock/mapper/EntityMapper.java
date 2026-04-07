package com.beautystock.mapper;

/**
 * Factory Pattern implementation for DTO mapping.
 * Provides generic contract for entity-to-DTO transformations.
 * Centralizes mapping logic and makes it testable and reusable.
 */
public interface EntityMapper<E, D> {
    /**
     * Convert entity to DTO.
     */
    D toDto(E entity);

    /**
     * Convert DTO to entity.
     */
    E toEntity(D dto);
}
