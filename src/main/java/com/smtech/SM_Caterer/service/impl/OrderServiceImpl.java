package com.smtech.SM_Caterer.service.impl;

import com.smtech.SM_Caterer.domain.entity.Customer;
import com.smtech.SM_Caterer.domain.entity.EventType;
import com.smtech.SM_Caterer.domain.entity.Order;
import com.smtech.SM_Caterer.domain.entity.Tenant;
import com.smtech.SM_Caterer.domain.enums.OrderStatus;
import com.smtech.SM_Caterer.domain.repository.CustomerRepository;
import com.smtech.SM_Caterer.domain.repository.EventTypeRepository;
import com.smtech.SM_Caterer.domain.repository.OrderRepository;
import com.smtech.SM_Caterer.domain.repository.TenantRepository;
import com.smtech.SM_Caterer.exception.DuplicateResourceException;
import com.smtech.SM_Caterer.exception.ResourceNotFoundException;
import com.smtech.SM_Caterer.service.OrderService;
import com.smtech.SM_Caterer.service.base.BaseServiceImpl;
import com.smtech.SM_Caterer.service.dto.OrderDTO;
import com.smtech.SM_Caterer.service.mapper.EntityMapper;
import com.smtech.SM_Caterer.service.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service implementation for Order operations.
 *
 * Business Logic:
 * - Order creation with menu items and utilities
 * - Total amount calculation
 * - Order status management
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class OrderServiceImpl extends BaseServiceImpl<Order, OrderDTO, Long>
        implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final TenantRepository tenantRepository;
    private final CustomerRepository customerRepository;
    private final EventTypeRepository eventTypeRepository;

    @Override
    protected JpaRepository<Order, Long> getRepository() {
        return orderRepository;
    }

    @Override
    protected EntityMapper<OrderDTO, Order> getMapper() {
        return orderMapper;
    }

    @Override
    protected String getEntityName() {
        return "Order";
    }

    @Override
    @Transactional
    public OrderDTO create(OrderDTO dto) {
        log.debug("Creating new order: {}", dto.getOrderNumber());

        // Validate unique constraint
        if (orderRepository.existsByTenantIdAndOrderNumber(dto.getTenantId(), dto.getOrderNumber())) {
            throw new DuplicateResourceException("Order", "orderNumber", dto.getOrderNumber());
        }

        Order entity = orderMapper.toEntity(dto);

        // Set tenant reference
        if (dto.getTenantId() != null) {
            Tenant tenant = tenantRepository.findById(dto.getTenantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Tenant", "id", dto.getTenantId()));
            entity.setTenant(tenant);
        }

        // Set customer reference
        if (dto.getCustomerId() != null) {
            Customer customer = customerRepository.findById(dto.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", dto.getCustomerId()));
            entity.setCustomer(customer);
        }

        // Set event type reference
        if (dto.getEventTypeId() != null) {
            EventType eventType = eventTypeRepository.findById(dto.getEventTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("EventType", "id", dto.getEventTypeId()));
            entity.setEventType(eventType);
        }

        Order saved = orderRepository.save(entity);
        log.info("Order created: {} (ID: {})", saved.getOrderNumber(), saved.getId());

        return orderMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OrderDTO> findByTenantIdAndOrderNumber(Long tenantId, String orderNumber) {
        return orderRepository.findByTenantIdAndOrderNumber(tenantId, orderNumber)
                .map(orderMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderDTO> findByTenantId(Long tenantId, Pageable pageable) {
        return orderRepository.findByTenantId(tenantId, pageable)
                .map(orderMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDTO> findByCustomerId(Long customerId) {
        return orderMapper.toDto(orderRepository.findByCustomerId(customerId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDTO> findByEventDate(LocalDate eventDate) {
        return orderMapper.toDto(orderRepository.findByEventDate(eventDate));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDTO> findByStatus(OrderStatus status) {
        return orderMapper.toDto(orderRepository.findByStatus(status));
    }
}
