package com.smtech.SM_Caterer.service.base;

import com.smtech.SM_Caterer.exception.ResourceNotFoundException;
import com.smtech.SM_Caterer.service.mapper.EntityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Base service implementation providing common CRUD operations.
 * All service implementations should extend this class.
 *
 * @param <E> Entity type
 * @param <DTO> DTO type
 * @param <ID> ID type
 */
@Slf4j
@RequiredArgsConstructor
public abstract class BaseServiceImpl<E, DTO, ID> implements BaseService<DTO, ID> {

    protected abstract JpaRepository<E, ID> getRepository();
    protected abstract EntityMapper<DTO, E> getMapper();
    protected abstract String getEntityName();

    @Override
    @Transactional
    public DTO create(DTO dto) {
        log.debug("Creating new {}", getEntityName());

        E entity = getMapper().toEntity(dto);
        E savedEntity = getRepository().save(entity);

        log.info("{} created with ID: {}", getEntityName(), savedEntity);
        return getMapper().toDto(savedEntity);
    }

    @Override
    @Transactional
    public DTO update(ID id, DTO dto) {
        log.debug("Updating {} with ID: {}", getEntityName(), id);

        if (!getRepository().existsById(id)) {
            throw new ResourceNotFoundException(getEntityName(), "id", id);
        }

        E entity = getMapper().toEntity(dto);
        E updatedEntity = getRepository().save(entity);

        log.info("{} updated with ID: {}", getEntityName(), id);
        return getMapper().toDto(updatedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DTO> findById(ID id) {
        log.debug("Finding {} by ID: {}", getEntityName(), id);
        return getRepository().findById(id)
                .map(getMapper()::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DTO> findAll(Pageable pageable) {
        log.debug("Finding all {} with pagination", getEntityName());
        return getRepository().findAll(pageable)
                .map(getMapper()::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DTO> findAll() {
        log.debug("Finding all {} (no pagination)", getEntityName());
        return getMapper().toDto(getRepository().findAll());
    }

    @Override
    @Transactional
    public void delete(ID id) {
        log.debug("Deleting {} with ID: {}", getEntityName(), id);

        if (!getRepository().existsById(id)) {
            throw new ResourceNotFoundException(getEntityName(), "id", id);
        }

        // Soft delete via @SQLDelete annotation
        getRepository().deleteById(id);

        log.info("{} deleted with ID: {}", getEntityName(), id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(ID id) {
        return getRepository().existsById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public long count() {
        return getRepository().count();
    }
}
