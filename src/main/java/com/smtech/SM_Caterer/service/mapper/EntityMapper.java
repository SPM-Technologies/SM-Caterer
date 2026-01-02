package com.smtech.SM_Caterer.service.mapper;

import java.util.List;

/**
 * Generic mapper interface for entity-DTO conversion.
 * MapStruct will generate implementations at compile time.
 *
 * @param <D> DTO type
 * @param <E> Entity type
 */
public interface EntityMapper<D, E> {

    /**
     * Converts DTO to entity.
     * @param dto DTO object
     * @return Entity object
     */
    E toEntity(D dto);

    /**
     * Converts entity to DTO.
     * @param entity Entity object
     * @return DTO object
     */
    D toDto(E entity);

    /**
     * Converts list of DTOs to list of entities.
     * @param dtoList List of DTO objects
     * @return List of entity objects
     */
    List<E> toEntity(List<D> dtoList);

    /**
     * Converts list of entities to list of DTOs.
     * @param entityList List of entity objects
     * @return List of DTO objects
     */
    List<D> toDto(List<E> entityList);
}
