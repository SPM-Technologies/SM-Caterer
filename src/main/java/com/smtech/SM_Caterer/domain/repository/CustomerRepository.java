package com.smtech.SM_Caterer.domain.repository;

import com.smtech.SM_Caterer.domain.entity.Customer;
import com.smtech.SM_Caterer.domain.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Customer entity.
 */
@Repository
public interface CustomerRepository extends BaseRepository<Customer, Long> {

    /**
     * Finds customer by tenant and customer code.
     * @param tenantId Tenant ID
     * @param customerCode Customer code
     * @return Customer if found
     */
    Optional<Customer> findByTenantIdAndCustomerCode(Long tenantId, String customerCode);

    /**
     * Finds customer by tenant and phone.
     * @param tenantId Tenant ID
     * @param phone Phone number
     * @return Customer if found
     */
    Optional<Customer> findByTenantIdAndPhone(Long tenantId, String phone);

    /**
     * Finds customers by phone.
     * @param phone Phone number
     * @return List of customers
     */
    List<Customer> findByPhone(String phone);

    /**
     * Finds customer by tenant and email.
     * @param tenantId Tenant ID
     * @param email Email
     * @return Customer if found
     */
    Optional<Customer> findByTenantIdAndEmail(Long tenantId, String email);

    /**
     * Finds all customers for tenant.
     * @param tenantId Tenant ID
     * @return List of customers
     */
    List<Customer> findByTenantId(Long tenantId);

    /**
     * Finds all customers for tenant with pagination.
     * @param tenantId Tenant ID
     * @param pageable Pagination parameters
     * @return Page of customers
     */
    Page<Customer> findByTenantId(Long tenantId, Pageable pageable);

    /**
     * Finds customers by tenant and status.
     * @param tenantId Tenant ID
     * @param status Status
     * @return List of customers
     */
    List<Customer> findByTenantIdAndStatus(Long tenantId, Status status);

    /**
     * Searches customers by name (case-insensitive).
     */
    @Query("SELECT c FROM Customer c WHERE c.tenant.id = :tenantId " +
           "AND LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Customer> searchByName(@Param("tenantId") Long tenantId, @Param("name") String name);

    /**
     * Searches customers by phone.
     */
    @Query("SELECT c FROM Customer c WHERE c.tenant.id = :tenantId " +
           "AND c.phone LIKE CONCAT('%', :phone, '%')")
    List<Customer> searchByPhone(@Param("tenantId") Long tenantId, @Param("phone") String phone);

    /**
     * Checks if customer code exists for tenant.
     */
    boolean existsByTenantIdAndCustomerCode(Long tenantId, String customerCode);

    /**
     * Checks if phone exists for tenant.
     */
    boolean existsByTenantIdAndPhone(Long tenantId, String phone);

    /**
     * Counts customers by tenant.
     */
    long countByTenantId(Long tenantId);

    /**
     * Counts customers by tenant and status.
     */
    long countByTenantIdAndStatus(Long tenantId, Status status);
}
