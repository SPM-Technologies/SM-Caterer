package com.smtech.SM_Caterer.service.impl;

import com.smtech.SM_Caterer.domain.entity.Menu;
import com.smtech.SM_Caterer.domain.entity.Tenant;
import com.smtech.SM_Caterer.domain.repository.MenuRepository;
import com.smtech.SM_Caterer.domain.repository.TenantRepository;
import com.smtech.SM_Caterer.exception.DuplicateResourceException;
import com.smtech.SM_Caterer.exception.ResourceNotFoundException;
import com.smtech.SM_Caterer.service.MenuService;
import com.smtech.SM_Caterer.service.base.BaseServiceImpl;
import com.smtech.SM_Caterer.service.dto.MenuDTO;
import com.smtech.SM_Caterer.service.mapper.EntityMapper;
import com.smtech.SM_Caterer.service.mapper.MenuMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service implementation for Menu operations.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MenuServiceImpl extends BaseServiceImpl<Menu, MenuDTO, Long>
        implements MenuService {

    private final MenuRepository menuRepository;
    private final MenuMapper menuMapper;
    private final TenantRepository tenantRepository;

    @Override
    protected JpaRepository<Menu, Long> getRepository() {
        return menuRepository;
    }

    @Override
    protected EntityMapper<MenuDTO, Menu> getMapper() {
        return menuMapper;
    }

    @Override
    protected String getEntityName() {
        return "Menu";
    }

    @Override
    @Transactional
    public MenuDTO create(MenuDTO dto) {
        log.debug("Creating new menu: {}", dto.getMenuCode());

        // Validate unique constraint
        if (menuRepository.existsByTenantIdAndMenuCode(dto.getTenantId(), dto.getMenuCode())) {
            throw new DuplicateResourceException("Menu", "menuCode", dto.getMenuCode());
        }

        Menu entity = menuMapper.toEntity(dto);

        // Set tenant reference
        if (dto.getTenantId() != null) {
            Tenant tenant = tenantRepository.findById(dto.getTenantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Tenant", "id", dto.getTenantId()));
            entity.setTenant(tenant);
        }

        Menu saved = menuRepository.save(entity);
        log.info("Menu created: {} (ID: {})", saved.getMenuCode(), saved.getId());

        return menuMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MenuDTO> findByTenantIdAndMenuCode(Long tenantId, String menuCode) {
        return menuRepository.findByTenantIdAndMenuCode(tenantId, menuCode)
                .map(menuMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuDTO> findByTenantId(Long tenantId) {
        return menuMapper.toDto(menuRepository.findByTenantId(tenantId));
    }
}
