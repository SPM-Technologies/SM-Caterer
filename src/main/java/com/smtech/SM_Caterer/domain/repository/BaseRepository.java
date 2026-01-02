package com.smtech.SM_Caterer.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * Base repository interface for all repositories.
 * Provides standard CRUD operations via JpaRepository.
 *
 * @param <T> Entity type
 * @param <ID> ID type (usually Long)
 */
@NoRepositoryBean
public interface BaseRepository<T, ID> extends JpaRepository<T, ID> {
    // All CRUD methods inherited from JpaRepository
}
