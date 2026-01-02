package com.smtech.SM_Caterer.service;

import com.smtech.SM_Caterer.service.base.BaseService;
import com.smtech.SM_Caterer.service.dto.OrderUtilityDTO;

import java.util.List;

/**
 * Service interface for OrderUtility operations.
 */
public interface OrderUtilityService extends BaseService<OrderUtilityDTO, Long> {

    List<OrderUtilityDTO> findByOrderId(Long orderId);

    List<OrderUtilityDTO> findByUtilityId(Long utilityId);
}
