package com.smtech.SM_Caterer.service;

import com.smtech.SM_Caterer.service.base.BaseService;
import com.smtech.SM_Caterer.service.dto.OrderMenuItemDTO;

import java.util.List;

/**
 * Service interface for OrderMenuItem operations.
 */
public interface OrderMenuItemService extends BaseService<OrderMenuItemDTO, Long> {

    List<OrderMenuItemDTO> findByOrderId(Long orderId);

    List<OrderMenuItemDTO> findByMenuId(Long menuId);
}
