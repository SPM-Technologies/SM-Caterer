package com.smtech.SM_Caterer.service.impl;

import com.smtech.SM_Caterer.domain.entity.Menu;
import com.smtech.SM_Caterer.domain.entity.Order;
import com.smtech.SM_Caterer.domain.entity.OrderMenuItem;
import com.smtech.SM_Caterer.domain.repository.MenuRepository;
import com.smtech.SM_Caterer.domain.repository.OrderMenuItemRepository;
import com.smtech.SM_Caterer.domain.repository.OrderRepository;
import com.smtech.SM_Caterer.exception.ResourceNotFoundException;
import com.smtech.SM_Caterer.service.OrderMenuItemService;
import com.smtech.SM_Caterer.service.base.BaseServiceImpl;
import com.smtech.SM_Caterer.service.dto.OrderMenuItemDTO;
import com.smtech.SM_Caterer.service.mapper.EntityMapper;
import com.smtech.SM_Caterer.service.mapper.OrderMenuItemMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service implementation for OrderMenuItem operations.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class OrderMenuItemServiceImpl extends BaseServiceImpl<OrderMenuItem, OrderMenuItemDTO, Long>
        implements OrderMenuItemService {

    private final OrderMenuItemRepository orderMenuItemRepository;
    private final OrderMenuItemMapper orderMenuItemMapper;
    private final OrderRepository orderRepository;
    private final MenuRepository menuRepository;

    @Override
    protected JpaRepository<OrderMenuItem, Long> getRepository() {
        return orderMenuItemRepository;
    }

    @Override
    protected EntityMapper<OrderMenuItemDTO, OrderMenuItem> getMapper() {
        return orderMenuItemMapper;
    }

    @Override
    protected String getEntityName() {
        return "OrderMenuItem";
    }

    @Override
    @Transactional
    public OrderMenuItemDTO create(OrderMenuItemDTO dto) {
        log.debug("Creating new order menu item for order ID: {} and menu ID: {}",
                dto.getOrderId(), dto.getMenuId());

        OrderMenuItem entity = orderMenuItemMapper.toEntity(dto);

        // Set order reference
        if (dto.getOrderId() != null) {
            Order order = orderRepository.findById(dto.getOrderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Order", "id", dto.getOrderId()));
            entity.setOrder(order);
        }

        // Set menu reference
        if (dto.getMenuId() != null) {
            Menu menu = menuRepository.findById(dto.getMenuId())
                    .orElseThrow(() -> new ResourceNotFoundException("Menu", "id", dto.getMenuId()));
            entity.setMenu(menu);
        }

        OrderMenuItem saved = orderMenuItemRepository.save(entity);
        log.info("OrderMenuItem created (ID: {})", saved.getId());

        return orderMenuItemMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderMenuItemDTO> findByOrderId(Long orderId) {
        return orderMenuItemMapper.toDto(orderMenuItemRepository.findByOrderId(orderId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderMenuItemDTO> findByMenuId(Long menuId) {
        return orderMenuItemMapper.toDto(orderMenuItemRepository.findByMenuId(menuId));
    }
}
