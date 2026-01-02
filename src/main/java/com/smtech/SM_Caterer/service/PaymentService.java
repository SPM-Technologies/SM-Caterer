package com.smtech.SM_Caterer.service;

import com.smtech.SM_Caterer.domain.enums.PaymentStatus;
import com.smtech.SM_Caterer.service.base.BaseService;
import com.smtech.SM_Caterer.service.dto.PaymentDTO;

import java.util.List;

/**
 * Service interface for Payment operations.
 */
public interface PaymentService extends BaseService<PaymentDTO, Long> {

    List<PaymentDTO> findByOrderId(Long orderId);

    List<PaymentDTO> findByTenantId(Long tenantId);

    List<PaymentDTO> findByStatus(PaymentStatus status);
}
