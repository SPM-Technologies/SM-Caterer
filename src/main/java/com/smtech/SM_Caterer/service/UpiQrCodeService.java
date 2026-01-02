package com.smtech.SM_Caterer.service;

import com.smtech.SM_Caterer.service.base.BaseService;
import com.smtech.SM_Caterer.service.dto.UpiQrCodeDTO;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for UpiQrCode operations.
 */
public interface UpiQrCodeService extends BaseService<UpiQrCodeDTO, Long> {

    List<UpiQrCodeDTO> findByTenantId(Long tenantId);

    Optional<UpiQrCodeDTO> findByUpiId(String upiId);
}
