package com.smtech.SM_Caterer.service.impl;

import com.smtech.SM_Caterer.domain.entity.Material;
import com.smtech.SM_Caterer.domain.entity.Menu;
import com.smtech.SM_Caterer.domain.entity.RecipeItem;
import com.smtech.SM_Caterer.domain.repository.MaterialRepository;
import com.smtech.SM_Caterer.domain.repository.MenuRepository;
import com.smtech.SM_Caterer.domain.repository.RecipeItemRepository;
import com.smtech.SM_Caterer.exception.ResourceNotFoundException;
import com.smtech.SM_Caterer.service.RecipeItemService;
import com.smtech.SM_Caterer.service.base.BaseServiceImpl;
import com.smtech.SM_Caterer.service.dto.RecipeItemDTO;
import com.smtech.SM_Caterer.service.mapper.EntityMapper;
import com.smtech.SM_Caterer.service.mapper.RecipeItemMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service implementation for RecipeItem operations.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RecipeItemServiceImpl extends BaseServiceImpl<RecipeItem, RecipeItemDTO, Long>
        implements RecipeItemService {

    private final RecipeItemRepository recipeItemRepository;
    private final RecipeItemMapper recipeItemMapper;
    private final MenuRepository menuRepository;
    private final MaterialRepository materialRepository;

    @Override
    protected JpaRepository<RecipeItem, Long> getRepository() {
        return recipeItemRepository;
    }

    @Override
    protected EntityMapper<RecipeItemDTO, RecipeItem> getMapper() {
        return recipeItemMapper;
    }

    @Override
    protected String getEntityName() {
        return "RecipeItem";
    }

    @Override
    @Transactional
    public RecipeItemDTO create(RecipeItemDTO dto) {
        log.debug("Creating new recipe item for menu ID: {} and material ID: {}",
                dto.getMenuId(), dto.getMaterialId());

        RecipeItem entity = recipeItemMapper.toEntity(dto);

        // Set menu reference
        if (dto.getMenuId() != null) {
            Menu menu = menuRepository.findById(dto.getMenuId())
                    .orElseThrow(() -> new ResourceNotFoundException("Menu", "id", dto.getMenuId()));
            entity.setMenu(menu);
        }

        // Set material reference
        if (dto.getMaterialId() != null) {
            Material material = materialRepository.findById(dto.getMaterialId())
                    .orElseThrow(() -> new ResourceNotFoundException("Material", "id", dto.getMaterialId()));
            entity.setMaterial(material);
        }

        RecipeItem saved = recipeItemRepository.save(entity);
        log.info("RecipeItem created (ID: {})", saved.getId());

        return recipeItemMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecipeItemDTO> findByMenuId(Long menuId) {
        return recipeItemMapper.toDto(recipeItemRepository.findByMenuId(menuId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecipeItemDTO> findByMaterialId(Long materialId) {
        return recipeItemMapper.toDto(recipeItemRepository.findByMaterialId(materialId));
    }
}
