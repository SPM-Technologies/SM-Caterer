package com.smtech.SM_Caterer.service.impl;

import com.smtech.SM_Caterer.domain.entity.Menu;
import com.smtech.SM_Caterer.domain.enums.LanguageCode;
import com.smtech.SM_Caterer.domain.entity.MenuTranslation;
import com.smtech.SM_Caterer.domain.repository.MenuRepository;
import com.smtech.SM_Caterer.domain.repository.MenuTranslationRepository;
import com.smtech.SM_Caterer.exception.DuplicateResourceException;
import com.smtech.SM_Caterer.exception.ResourceNotFoundException;
import com.smtech.SM_Caterer.service.MenuTranslationService;
import com.smtech.SM_Caterer.service.base.BaseServiceImpl;
import com.smtech.SM_Caterer.service.dto.MenuTranslationDTO;
import com.smtech.SM_Caterer.service.mapper.EntityMapper;
import com.smtech.SM_Caterer.service.mapper.MenuTranslationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service implementation for MenuTranslation operations.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MenuTranslationServiceImpl extends BaseServiceImpl<MenuTranslation, MenuTranslationDTO, Long>
        implements MenuTranslationService {

    private final MenuTranslationRepository menuTranslationRepository;
    private final MenuTranslationMapper menuTranslationMapper;
    private final MenuRepository menuRepository;

    @Override
    protected JpaRepository<MenuTranslation, Long> getRepository() {
        return menuTranslationRepository;
    }

    @Override
    protected EntityMapper<MenuTranslationDTO, MenuTranslation> getMapper() {
        return menuTranslationMapper;
    }

    @Override
    protected String getEntityName() {
        return "MenuTranslation";
    }

    @Override
    @Transactional
    public MenuTranslationDTO create(MenuTranslationDTO dto) {
        log.debug("Creating new menu translation for menu ID: {} in language: {}",
                dto.getMenuId(), dto.getLanguageCode());

        // Validate unique constraint
        if (menuTranslationRepository.existsByMenuIdAndLanguageCode(
                dto.getMenuId(), dto.getLanguageCode())) {
            throw new DuplicateResourceException("MenuTranslation",
                    "menuId+languageCode", dto.getMenuId() + "+" + dto.getLanguageCode());
        }

        MenuTranslation entity = menuTranslationMapper.toEntity(dto);

        // Set menu reference
        if (dto.getMenuId() != null) {
            Menu menu = menuRepository.findById(dto.getMenuId())
                    .orElseThrow(() -> new ResourceNotFoundException("Menu", "id", dto.getMenuId()));
            entity.setMenu(menu);
        }

        MenuTranslation saved = menuTranslationRepository.save(entity);
        log.info("MenuTranslation created (ID: {})", saved.getId());

        return menuTranslationMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MenuTranslationDTO> findByMenuIdAndLanguageCode(Long menuId, LanguageCode languageCode) {
        return menuTranslationRepository.findByMenuIdAndLanguageCode(menuId, languageCode)
                .map(menuTranslationMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuTranslationDTO> findByMenuId(Long menuId) {
        return menuTranslationMapper.toDto(menuTranslationRepository.findByMenuId(menuId));
    }

    @Transactional(readOnly = true)
    public List<MenuTranslationDTO> findByLanguageCode(LanguageCode languageCode) {
        return menuTranslationMapper.toDto(menuTranslationRepository.findByLanguageCode(languageCode));
    }
}
