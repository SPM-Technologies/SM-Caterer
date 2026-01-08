package com.smtech.SM_Caterer.domain.repository;

import com.smtech.SM_Caterer.domain.entity.EmailLog;
import com.smtech.SM_Caterer.domain.enums.EmailStatus;
import com.smtech.SM_Caterer.domain.enums.EmailType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for EmailLog entity.
 */
@Repository
public interface EmailLogRepository extends BaseRepository<EmailLog, Long> {

    /**
     * Finds all email logs for tenant.
     */
    List<EmailLog> findByTenantId(Long tenantId);

    /**
     * Finds email logs for tenant with pagination.
     */
    Page<EmailLog> findByTenantId(Long tenantId, Pageable pageable);

    /**
     * Finds email logs by status.
     */
    List<EmailLog> findByStatus(EmailStatus status);

    /**
     * Finds email logs by tenant and status.
     */
    List<EmailLog> findByTenantIdAndStatus(Long tenantId, EmailStatus status);

    /**
     * Finds email logs by reference.
     */
    @Query("SELECT e FROM EmailLog e WHERE e.tenant.id = :tenantId AND e.referenceType = :referenceType AND e.referenceId = :referenceId")
    List<EmailLog> findByReference(@Param("tenantId") Long tenantId,
                                   @Param("referenceType") String referenceType,
                                   @Param("referenceId") Long referenceId);

    /**
     * Finds pending emails that can be retried.
     */
    @Query("SELECT e FROM EmailLog e WHERE e.status = 'FAILED' AND e.retryCount < 3")
    List<EmailLog> findRetryableEmails();

    /**
     * Finds emails sent within date range.
     */
    @Query("SELECT e FROM EmailLog e WHERE e.tenant.id = :tenantId AND e.sentAt BETWEEN :startDate AND :endDate")
    List<EmailLog> findByDateRange(@Param("tenantId") Long tenantId,
                                   @Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate);

    /**
     * Counts emails by tenant and status.
     */
    long countByTenantIdAndStatus(Long tenantId, EmailStatus status);

    /**
     * Counts emails by tenant and type.
     */
    long countByTenantIdAndEmailType(Long tenantId, EmailType emailType);
}
