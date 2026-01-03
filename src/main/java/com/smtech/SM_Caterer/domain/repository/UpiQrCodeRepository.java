package com.smtech.SM_Caterer.domain.repository;

import com.smtech.SM_Caterer.domain.entity.UpiQrCode;
import com.smtech.SM_Caterer.domain.enums.UpiQrCodeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for UpiQrCode entity.
 */
@Repository
public interface UpiQrCodeRepository extends BaseRepository<UpiQrCode, Long> {

    /**
     * Finds UPI QR code by tenant and UPI ID.
     * @param tenantId Tenant ID
     * @param upiId UPI ID
     * @return UpiQrCode if found
     */
    Optional<UpiQrCode> findByTenantIdAndUpiId(Long tenantId, String upiId);

    /**
     * Finds UPI QR code by UPI ID.
     * @param upiId UPI ID
     * @return UpiQrCode if found
     */
    Optional<UpiQrCode> findByUpiId(String upiId);

    /**
     * Finds all UPI QR codes for tenant.
     * @param tenantId Tenant ID
     * @return List of UPI QR codes
     */
    List<UpiQrCode> findByTenantId(Long tenantId);

    /**
     * Finds all UPI QR codes for tenant with pagination.
     * @param tenantId Tenant ID
     * @param pageable Pagination parameters
     * @return Page of UPI QR codes
     */
    Page<UpiQrCode> findByTenantId(Long tenantId, Pageable pageable);

    /**
     * Finds UPI QR codes by tenant and status.
     * @param tenantId Tenant ID
     * @param status QR code status
     * @return List of UPI QR codes
     */
    List<UpiQrCode> findByTenantIdAndStatus(Long tenantId, UpiQrCodeStatus status);

    /**
     * Checks if UPI ID exists for tenant.
     * @param tenantId Tenant ID
     * @param upiId UPI ID
     * @return true if exists
     */
    boolean existsByTenantIdAndUpiId(Long tenantId, String upiId);

    /**
     * Counts UPI QR codes by tenant.
     */
    long countByTenantId(Long tenantId);

    /**
     * Counts UPI QR codes by tenant and status.
     */
    long countByTenantIdAndStatus(Long tenantId, UpiQrCodeStatus status);
}
