package com.smtech.SM_Caterer.domain.repository;

import com.smtech.SM_Caterer.domain.entity.EventType;
import com.smtech.SM_Caterer.domain.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for EventType entity.
 */
@Repository
public interface EventTypeRepository extends BaseRepository<EventType, Long> {

    /**
     * Finds event type by tenant and event code.
     * @param tenantId Tenant ID
     * @param eventCode Event code
     * @return EventType if found
     */
    Optional<EventType> findByTenantIdAndEventCode(Long tenantId, String eventCode);

    /**
     * Finds all event types for tenant with translations loaded.
     * Uses EntityGraph to prevent N+1 queries.
     */
    @EntityGraph(attributePaths = {"translations"})
    List<EventType> findByTenantId(Long tenantId);

    /**
     * Paginated query with eager loading.
     */
    @EntityGraph(attributePaths = {"translations"})
    Page<EventType> findByTenantId(Long tenantId, Pageable pageable);

    /**
     * Finds event types by tenant and status.
     * @param tenantId Tenant ID
     * @param status Status
     * @return List of event types
     */
    List<EventType> findByTenantIdAndStatus(Long tenantId, Status status);

    /**
     * Checks if event code exists for tenant.
     * @param tenantId Tenant ID
     * @param eventCode Event code
     * @return true if exists
     */
    boolean existsByTenantIdAndEventCode(Long tenantId, String eventCode);

    /**
     * Counts event types by tenant.
     */
    long countByTenantId(Long tenantId);

    /**
     * Counts event types by tenant and status.
     */
    long countByTenantIdAndStatus(Long tenantId, Status status);
}
