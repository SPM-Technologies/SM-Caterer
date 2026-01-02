package com.smtech.SM_Caterer.service.impl;

import com.smtech.SM_Caterer.domain.entity.Order;
import com.smtech.SM_Caterer.domain.entity.OrderUtility;
import com.smtech.SM_Caterer.domain.entity.Utility;
import com.smtech.SM_Caterer.domain.repository.OrderRepository;
import com.smtech.SM_Caterer.domain.repository.OrderUtilityRepository;
import com.smtech.SM_Caterer.domain.repository.UtilityRepository;
import com.smtech.SM_Caterer.exception.ResourceNotFoundException;
import com.smtech.SM_Caterer.service.OrderUtilityService;
import com.smtech.SM_Caterer.service.base.BaseServiceImpl;
import com.smtech.SM_Caterer.service.dto.OrderUtilityDTO;
import com.smtech.SM_Caterer.service.mapper.EntityMapper;
import com.smtech.SM_Caterer.service.mapper.OrderUtilityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service implementation for OrderUtility operations.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class OrderUtilityServiceImpl extends BaseServiceImpl<OrderUtility, OrderUtilityDTO, Long>
        implements OrderUtilityService {

    private final OrderUtilityRepository orderUtilityRepository;
    private final OrderUtilityMapper orderUtilityMapper;
    private final OrderRepository orderRepository;
    private final UtilityRepository utilityRepository;

    @Override
    protected JpaRepository<OrderUtility, Long> getRepository() {
        return orderUtilityRepository;
    }

    @Override
    protected EntityMapper<OrderUtilityDTO, OrderUtility> getMapper() {
        return orderUtilityMapper;
    }

    @Override
    protected String getEntityName() {
        return "OrderUtility";
    }

    @Override
    @Transactional
    public OrderUtilityDTO create(OrderUtilityDTO dto) {
        log.debug("Creating new order utility for order ID: {} and utility ID: {}",
                dto.getOrderId(), dto.getUtilityId());

        OrderUtility entity = orderUtilityMapper.toEntity(dto);

        // Set order reference
        if (dto.getOrderId() != null) {
            Order order = orderRepository.findById(dto.getOrderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Order", "id", dto.getOrderId()));
            entity.setOrder(order);
        }

        // Set utility reference
        if (dto.getUtilityId() != null) {
            Utility utility = utilityRepository.findById(dto.getUtilityId())
                    .orElseThrow(() -> new ResourceNotFoundException("Utility", "id", dto.getUtilityId()));
            entity.setUtility(utility);
        }

        OrderUtility saved = orderUtilityRepository.save(entity);
        log.info("OrderUtility created (ID: {})", saved.getId());

        return orderUtilityMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderUtilityDTO> findByOrderId(Long orderId) {
        return orderUtilityMapper.toDto(orderUtilityRepository.findByOrderId(orderId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderUtilityDTO> findByUtilityId(Long utilityId) {
        return orderUtilityMapper.toDto(orderUtilityRepository.findByUtilityId(utilityId));
    }
}
