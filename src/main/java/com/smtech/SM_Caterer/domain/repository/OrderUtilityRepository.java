package com.smtech.SM_Caterer.domain.repository;

import com.smtech.SM_Caterer.domain.entity.OrderUtility;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for OrderUtility entity.
 */
@Repository
public interface OrderUtilityRepository extends BaseRepository<OrderUtility, Long> {

    /**
     * Finds all utilities for order.
     * @param orderId Order ID
     * @return List of utilities
     */
    List<OrderUtility> findByOrderId(Long orderId);

    /**
     * Finds utilities by order and utility.
     * @param orderId Order ID
     * @param utilityId Utility ID
     * @return List of utilities
     */
    List<OrderUtility> findByOrderIdAndUtilityId(Long orderId, Long utilityId);

    /**
     * Finds utilities with utility details for order.
     * Used for calculating total cost.
     */
    @Query("SELECT ou FROM OrderUtility ou " +
           "JOIN FETCH ou.utility u " +
           "WHERE ou.order.id = :orderId")
    List<OrderUtility> findByOrderIdWithUtilityDetails(@Param("orderId") Long orderId);

    /**
     * Deletes all utilities for order.
     */
    void deleteByOrderId(Long orderId);

    /**
     * Counts utilities for order.
     */
    long countByOrderId(Long orderId);

    /**
     * Finds all order utilities for utility.
     * @param utilityId Utility ID
     * @return List of order utilities
     */
    List<OrderUtility> findByUtilityId(Long utilityId);

    /**
     * Finds orders containing specific utility by tenant.
     * Used to check if utility is in use before deletion.
     */
    @Query("SELECT ou FROM OrderUtility ou " +
           "WHERE ou.utility.id = :utilityId " +
           "AND ou.order.tenant.id = :tenantId")
    List<OrderUtility> findByTenantIdAndUtilityId(@Param("tenantId") Long tenantId, @Param("utilityId") Long utilityId);
}
