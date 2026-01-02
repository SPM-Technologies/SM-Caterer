package com.smtech.SM_Caterer.service;

import com.smtech.SM_Caterer.domain.enums.OrderStatus;
import com.smtech.SM_Caterer.service.base.BaseService;
import com.smtech.SM_Caterer.service.dto.OrderDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for Order operations.
 */
public interface OrderService extends BaseService<OrderDTO, Long> {

    Optional<OrderDTO> findByTenantIdAndOrderNumber(Long tenantId, String orderNumber);

    Page<OrderDTO> findByTenantId(Long tenantId, Pageable pageable);

    List<OrderDTO> findByCustomerId(Long customerId);

    List<OrderDTO> findByEventDate(LocalDate eventDate);

    List<OrderDTO> findByStatus(OrderStatus status);
}
