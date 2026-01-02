package com.smtech.SM_Caterer.domain.repository;

import com.smtech.SM_Caterer.domain.entity.RecipeItem;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for RecipeItem entity.
 */
@Repository
public interface RecipeItemRepository extends BaseRepository<RecipeItem, Long> {

    /**
     * Finds all recipe items for menu.
     * @param menuId Menu ID
     * @return List of recipe items
     */
    List<RecipeItem> findByMenuId(Long menuId);

    /**
     * Finds all recipe items for material.
     * @param materialId Material ID
     * @return List of recipe items
     */
    List<RecipeItem> findByMaterialId(Long materialId);

    /**
     * Finds recipe items by menu and material.
     * @param menuId Menu ID
     * @param materialId Material ID
     * @return List of recipe items
     */
    List<RecipeItem> findByMenuIdAndMaterialId(Long menuId, Long materialId);

    /**
     * Deletes all recipe items for menu.
     */
    void deleteByMenuId(Long menuId);

    /**
     * Counts recipe items for menu.
     */
    long countByMenuId(Long menuId);

    /**
     * Finds recipe items with material details for menu.
     * Used for calculating total cost.
     */
    @Query("SELECT ri FROM RecipeItem ri " +
           "JOIN FETCH ri.material m " +
           "JOIN FETCH m.unit " +
           "WHERE ri.menu.id = :menuId")
    List<RecipeItem> findByMenuIdWithMaterialDetails(@Param("menuId") Long menuId);
}
