package com.smtech.SM_Caterer.service.base;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Base service interface with CRUD operations.
 * All service interfaces should extend this.
 *
 * @param <DTO> DTO type
 * @param <ID> ID type (usually Long)
 */
public interface BaseService<DTO, ID> {

    /**
     * Creates new entity.
     * @param dto Entity data
     * @return Created entity with ID
     */
    DTO create(DTO dto);

    /**
     * Updates existing entity.
     * @param id Entity ID
     * @param dto Updated data
     * @return Updated entity
     * @throws com.smtech.SM_Caterer.exception.ResourceNotFoundException if not found
     */
    DTO update(ID id, DTO dto);

    /**
     * Finds entity by ID.
     * @param id Entity ID
     * @return Entity if found
     */
    Optional<DTO> findById(ID id);

    /**
     * Finds all entities (paginated).
     * @param pageable Pagination parameters
     * @return Page of entities
     */
    Page<DTO> findAll(Pageable pageable);

    /**
     * Finds all entities (no pagination).
     * WARNING: Use with caution on large datasets.
     * @return List of all entities
     */
    List<DTO> findAll();

    /**
     * Deletes entity by ID (soft delete).
     * @param id Entity ID
     * @throws com.smtech.SM_Caterer.exception.ResourceNotFoundException if not found
     */
    void delete(ID id);

    /**
     * Checks if entity exists.
     * @param id Entity ID
     * @return true if exists
     */
    boolean existsById(ID id);

    /**
     * Counts total entities.
     * @return Total count
     */
    long count();
}
