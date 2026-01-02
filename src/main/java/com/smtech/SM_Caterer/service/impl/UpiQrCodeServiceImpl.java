package com.smtech.SM_Caterer.service.impl;

import com.smtech.SM_Caterer.domain.entity.Tenant;
import com.smtech.SM_Caterer.domain.entity.UpiQrCode;
import com.smtech.SM_Caterer.domain.repository.TenantRepository;
import com.smtech.SM_Caterer.domain.repository.UpiQrCodeRepository;
import com.smtech.SM_Caterer.exception.ResourceNotFoundException;
import com.smtech.SM_Caterer.service.UpiQrCodeService;
import com.smtech.SM_Caterer.service.base.BaseServiceImpl;
import com.smtech.SM_Caterer.service.dto.UpiQrCodeDTO;
import com.smtech.SM_Caterer.service.mapper.EntityMapper;
import com.smtech.SM_Caterer.service.mapper.UpiQrCodeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service implementation for UpiQrCode operations.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UpiQrCodeServiceImpl extends BaseServiceImpl<UpiQrCode, UpiQrCodeDTO, Long>
        implements UpiQrCodeService {

    private final UpiQrCodeRepository upiQrCodeRepository;
    private final UpiQrCodeMapper upiQrCodeMapper;
    private final TenantRepository tenantRepository;

    @Override
    protected JpaRepository<UpiQrCode, Long> getRepository() {
        return upiQrCodeRepository;
    }

    @Override
    protected EntityMapper<UpiQrCodeDTO, UpiQrCode> getMapper() {
        return upiQrCodeMapper;
    }

    @Override
    protected String getEntityName() {
        return "UpiQrCode";
    }

    @Override
    @Transactional
    public UpiQrCodeDTO create(UpiQrCodeDTO dto) {
        log.debug("Creating new UPI QR code for tenant ID: {}", dto.getTenantId());

        UpiQrCode entity = upiQrCodeMapper.toEntity(dto);

        // Set tenant reference
        if (dto.getTenantId() != null) {
            Tenant tenant = tenantRepository.findById(dto.getTenantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Tenant", "id", dto.getTenantId()));
            entity.setTenant(tenant);
        }

        UpiQrCode saved = upiQrCodeRepository.save(entity);
        log.info("UpiQrCode created (ID: {}) for tenant ID: {}", saved.getId(), dto.getTenantId());

        return upiQrCodeMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UpiQrCodeDTO> findByTenantId(Long tenantId) {
        return upiQrCodeMapper.toDto(upiQrCodeRepository.findByTenantId(tenantId));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UpiQrCodeDTO> findByUpiId(String upiId) {
        return upiQrCodeRepository.findByUpiId(upiId)
                .map(upiQrCodeMapper::toDto);
    }
}
