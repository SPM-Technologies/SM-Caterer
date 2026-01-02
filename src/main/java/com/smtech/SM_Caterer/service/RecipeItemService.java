package com.smtech.SM_Caterer.service;

import com.smtech.SM_Caterer.service.base.BaseService;
import com.smtech.SM_Caterer.service.dto.RecipeItemDTO;

import java.util.List;

/**
 * Service interface for RecipeItem operations.
 */
public interface RecipeItemService extends BaseService<RecipeItemDTO, Long> {

    List<RecipeItemDTO> findByMenuId(Long menuId);

    List<RecipeItemDTO> findByMaterialId(Long materialId);
}
