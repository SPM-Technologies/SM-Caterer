package com.smtech.SM_Caterer.service.impl;

import com.smtech.SM_Caterer.domain.entity.*;
import com.smtech.SM_Caterer.domain.enums.OrderStatus;
import com.smtech.SM_Caterer.domain.enums.Status;
import com.smtech.SM_Caterer.domain.repository.*;
import com.smtech.SM_Caterer.exception.DuplicateResourceException;
import com.smtech.SM_Caterer.exception.ResourceNotFoundException;
import com.smtech.SM_Caterer.service.CustomerService;
import com.smtech.SM_Caterer.service.OrderNumberGeneratorService;
import com.smtech.SM_Caterer.service.OrderService;
import com.smtech.SM_Caterer.service.base.BaseServiceImpl;
import com.smtech.SM_Caterer.service.dto.*;
import com.smtech.SM_Caterer.service.mapper.EntityMapper;
import com.smtech.SM_Caterer.service.mapper.OrderMapper;
import com.smtech.SM_Caterer.web.dto.CustomerQuickCreateDTO;
import com.smtech.SM_Caterer.web.dto.OrderFormDTO;
import com.smtech.SM_Caterer.web.dto.OrderMenuItemFormDTO;
import com.smtech.SM_Caterer.web.dto.OrderUtilityFormDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service implementation for Order operations.
 *
 * Business Logic:
 * - Order creation with menu items and utilities
 * - Total amount calculation
 * - Order status management and workflow
 * - Price calculation with discount and tax
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
    private final MenuRepository menuRepository;
    private final UtilityRepository utilityRepository;
    private final OrderNumberGeneratorService orderNumberGeneratorService;
    private final CustomerService customerService;

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

    // ===== Basic CRUD Override =====

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

    // ===== Basic Queries =====

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

    // ===== Detail View =====

    @Override
    @Transactional(readOnly = true)
    public Optional<OrderDetailDTO> findByIdWithDetails(Long orderId) {
        return orderRepository.findByIdWithDetails(orderId)
                .map(this::mapToOrderDetailDTO);
    }

    /**
     * Maps Order entity to OrderDetailDTO with all related data.
     */
    private OrderDetailDTO mapToOrderDetailDTO(Order order) {
        OrderDetailDTO dto = OrderDetailDTO.builder()
                .id(order.getId())
                .tenantId(order.getTenant() != null ? order.getTenant().getId() : null)
                .orderNumber(order.getOrderNumber())
                // Customer info
                .customerId(order.getCustomer() != null ? order.getCustomer().getId() : null)
                .customerCode(order.getCustomer() != null ? order.getCustomer().getCustomerCode() : null)
                .customerName(order.getCustomer() != null ? order.getCustomer().getName() : null)
                .customerPhone(order.getCustomer() != null ? order.getCustomer().getPhone() : null)
                .customerEmail(order.getCustomer() != null ? order.getCustomer().getEmail() : null)
                .customerAddress(order.getCustomer() != null ? order.getCustomer().getAddress() : null)
                // Event info
                .eventTypeId(order.getEventType() != null ? order.getEventType().getId() : null)
                .eventTypeCode(order.getEventType() != null ? order.getEventType().getEventCode() : null)
                .eventTypeName(order.getEventType() != null ? order.getEventType().getEventCode() : null) // Will use translation in view
                .eventDate(order.getEventDate())
                .eventTime(order.getEventTime())
                .venueName(order.getVenueName())
                .venueAddress(order.getVenueAddress())
                .guestCount(order.getGuestCount())
                // Pricing
                .menuSubtotal(order.getMenuSubtotal())
                .utilitySubtotal(order.getUtilitySubtotal())
                .totalAmount(order.getTotalAmount())
                .discountPercent(order.getDiscountPercent())
                .discountAmount(order.getDiscountAmount())
                .taxPercent(order.getTaxPercent())
                .taxAmount(order.getTaxAmount())
                .grandTotal(order.getGrandTotal())
                .advanceAmount(order.getAdvanceAmount())
                .balanceAmount(order.getBalanceAmount())
                // Status & workflow
                .status(order.getStatus())
                .notes(order.getNotes())
                // Audit info
                .createdBy(order.getCreatedBy())
                .createdByName(order.getCreatedByUser() != null ? order.getCreatedByUser().getFullName() : null)
                .createdAt(order.getCreatedAt())
                .submittedAt(order.getSubmittedAt())
                .submittedBy(order.getSubmittedBy())
                .submittedByName(order.getSubmittedByUser() != null ? order.getSubmittedByUser().getFullName() : null)
                .approvedAt(order.getApprovedAt())
                .approvedBy(order.getApprovedBy())
                .approvedByName(order.getApprovedByUser() != null ? order.getApprovedByUser().getFullName() : null)
                .cancelledAt(order.getCancelledAt())
                .cancelledBy(order.getCancelledBy())
                .cancelledByName(order.getCancelledByUser() != null ? order.getCancelledByUser().getFullName() : null)
                .cancellationReason(order.getCancellationReason())
                .completedAt(order.getCompletedAt())
                .completedBy(order.getCompletedBy())
                .completedByName(order.getCompletedByUser() != null ? order.getCompletedByUser().getFullName() : null)
                .build();

        // Map menu items
        if (order.getMenuItems() != null) {
            dto.setMenuItems(order.getMenuItems().stream()
                    .map(this::mapToOrderMenuItemDTO)
                    .collect(Collectors.toList()));
        }

        // Map utilities
        if (order.getUtilities() != null) {
            dto.setUtilities(order.getUtilities().stream()
                    .map(this::mapToOrderUtilityDTO)
                    .collect(Collectors.toList()));
        }

        // Map payments
        if (order.getPayments() != null) {
            dto.setPayments(order.getPayments().stream()
                    .map(this::mapToPaymentDTO)
                    .collect(Collectors.toList()));
            dto.setTotalPaid(order.getTotalPaid());
        }

        return dto;
    }

    private OrderMenuItemDTO mapToOrderMenuItemDTO(OrderMenuItem item) {
        OrderMenuItemDTO dto = new OrderMenuItemDTO();
        dto.setId(item.getId());
        dto.setMenuId(item.getMenu() != null ? item.getMenu().getId() : null);
        dto.setMenuCode(item.getMenu() != null ? item.getMenu().getMenuCode() : null);
        dto.setMenuName(item.getMenu() != null ? item.getMenu().getMenuCode() : null);
        dto.setQuantity(item.getQuantity());
        dto.setPricePerItem(item.getPricePerItem());
        dto.setSubtotal(item.getSubtotal());
        return dto;
    }

    private OrderUtilityDTO mapToOrderUtilityDTO(OrderUtility item) {
        OrderUtilityDTO dto = new OrderUtilityDTO();
        dto.setId(item.getId());
        dto.setUtilityId(item.getUtility() != null ? item.getUtility().getId() : null);
        dto.setUtilityCode(item.getUtility() != null ? item.getUtility().getUtilityCode() : null);
        dto.setUtilityName(item.getUtility() != null ? item.getUtility().getUtilityCode() : null);
        dto.setQuantity(item.getQuantity());
        dto.setPricePerUnit(item.getPricePerItem());
        dto.setSubtotal(item.getSubtotal());
        return dto;
    }

    private PaymentDTO mapToPaymentDTO(Payment payment) {
        PaymentDTO dto = new PaymentDTO();
        dto.setId(payment.getId());
        dto.setPaymentDate(payment.getPaymentDate());
        dto.setAmount(payment.getAmount());
        dto.setPaymentMethod(payment.getPaymentMethod());
        dto.setTransactionReference(payment.getTransactionReference());
        dto.setNotes(payment.getNotes());
        dto.setStatus(payment.getStatus());
        return dto;
    }

    // ===== Search & Filter =====

    @Override
    @Transactional(readOnly = true)
    public Page<OrderDTO> searchOrders(Long tenantId, OrderSearchCriteria criteria, Pageable pageable) {
        return orderRepository.searchOrders(
                tenantId,
                criteria.getStatus(),
                criteria.getCustomerName(),
                criteria.getOrderNumber(),
                criteria.getEventDateFrom(),
                criteria.getEventDateTo(),
                pageable
        ).map(orderMapper::toDto);
    }

    // ===== Order Creation from Wizard =====

    @Override
    @Transactional
    public OrderDTO createFromForm(OrderFormDTO formDTO, Long tenantId, Long userId) {
        log.debug("Creating order from wizard form for tenant {}", tenantId);

        // Validate form
        if (!formDTO.isAllStepsValid()) {
            throw new IllegalArgumentException("Order form is not complete or invalid");
        }

        // Get or create customer
        Long customerId = resolveCustomerId(formDTO, tenantId);

        // Generate order number
        String orderNumber = orderNumberGeneratorService.generateOrderNumber(tenantId);

        // Get tenant
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", "id", tenantId));

        // Get customer
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));

        // Get event type
        EventType eventType = eventTypeRepository.findById(formDTO.getEventTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("EventType", "id", formDTO.getEventTypeId()));

        // Recalculate totals
        formDTO.recalculateTotals();

        // Create order entity
        Order order = Order.builder()
                .tenant(tenant)
                .orderNumber(orderNumber)
                .customer(customer)
                .eventType(eventType)
                .eventDate(formDTO.getEventDate())
                .eventTime(formDTO.getEventTime())
                .venueName(formDTO.getVenueName())
                .venueAddress(formDTO.getVenueAddress())
                .guestCount(formDTO.getGuestCount())
                .menuSubtotal(formDTO.getMenuSubtotal())
                .utilitySubtotal(formDTO.getUtilitySubtotal())
                .totalAmount(formDTO.getSubtotal())
                .discountPercent(formDTO.getDiscountPercent())
                .discountAmount(formDTO.getDiscountAmount())
                .taxPercent(formDTO.getTaxPercent())
                .taxAmount(formDTO.getTaxAmount())
                .grandTotal(formDTO.getGrandTotal())
                .balanceAmount(formDTO.getGrandTotal()) // Initially balance = grand total
                .status(OrderStatus.DRAFT)
                .notes(formDTO.getNotes())
                .build();

        order.setCreatedBy(userId);

        // Add menu items
        for (OrderMenuItemFormDTO menuItemDTO : formDTO.getMenuItems()) {
            Menu menu = menuRepository.findById(menuItemDTO.getMenuId())
                    .orElseThrow(() -> new ResourceNotFoundException("Menu", "id", menuItemDTO.getMenuId()));

            OrderMenuItem menuItem = OrderMenuItem.builder()
                    .menu(menu)
                    .quantity(menuItemDTO.getQuantity())
                    .pricePerItem(menuItemDTO.getPricePerItem())
                    .subtotal(menuItemDTO.getSubtotal())
                    .build();

            order.addMenuItem(menuItem);
        }

        // Add utilities
        for (OrderUtilityFormDTO utilityDTO : formDTO.getUtilities()) {
            Utility utility = utilityRepository.findById(utilityDTO.getUtilityId())
                    .orElseThrow(() -> new ResourceNotFoundException("Utility", "id", utilityDTO.getUtilityId()));

            OrderUtility orderUtility = OrderUtility.builder()
                    .utility(utility)
                    .quantity(utilityDTO.getQuantity())
                    .pricePerItem(utilityDTO.getPricePerUnit())
                    .subtotal(utilityDTO.getSubtotal())
                    .build();

            order.addUtility(orderUtility);
        }

        Order saved = orderRepository.save(order);
        log.info("Order created from wizard: {} (ID: {})", saved.getOrderNumber(), saved.getId());

        return orderMapper.toDto(saved);
    }

    /**
     * Resolves customer ID - either returns existing or creates new customer.
     */
    private Long resolveCustomerId(OrderFormDTO formDTO, Long tenantId) {
        if (!formDTO.isCreateNewCustomer()) {
            // Use existing customer
            if (formDTO.getCustomerId() == null) {
                throw new IllegalArgumentException("Customer ID is required");
            }
            return formDTO.getCustomerId();
        }

        // Create new customer from quick create form
        CustomerQuickCreateDTO newCustomer = formDTO.getNewCustomer();
        if (newCustomer == null || !newCustomer.isValid()) {
            throw new IllegalArgumentException("New customer data is invalid");
        }

        // Generate customer code
        String customerCode = generateCustomerCode(newCustomer.getPhone());

        CustomerDTO customerDTO = CustomerDTO.builder()
                .tenantId(tenantId)
                .customerCode(customerCode)
                .name(newCustomer.getName())
                .phone(newCustomer.getPhone())
                .email(newCustomer.getEmail())
                .address(newCustomer.getAddress())
                .city(newCustomer.getCity())
                .state(newCustomer.getState())
                .pincode(newCustomer.getPincode())
                .status(Status.ACTIVE)
                .build();

        CustomerDTO created = customerService.create(customerDTO);
        log.info("Quick-created customer {} for order", created.getCustomerCode());

        return created.getId();
    }

    /**
     * Generates customer code from phone number.
     */
    private String generateCustomerCode(String phone) {
        // Use last 4 digits of phone + timestamp suffix
        String phoneSuffix = phone.substring(phone.length() - 4);
        String timestamp = String.valueOf(System.currentTimeMillis() % 10000);
        return "CUST-" + phoneSuffix + "-" + timestamp;
    }

    @Override
    @Transactional
    public OrderDTO updateFromForm(Long orderId, OrderFormDTO formDTO, Long userId) {
        log.debug("Updating order {} from wizard form", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        // Validate status allows editing
        if (!order.isEditable()) {
            throw new IllegalStateException("Order cannot be edited in status: " + order.getStatus());
        }

        // Validate form
        if (!formDTO.isAllStepsValid()) {
            throw new IllegalArgumentException("Order form is not complete or invalid");
        }

        // Update customer if changed
        if (!formDTO.getCustomerId().equals(order.getCustomer().getId())) {
            Customer customer = customerRepository.findById(formDTO.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", formDTO.getCustomerId()));
            order.setCustomer(customer);
        }

        // Update event type if changed
        if (!formDTO.getEventTypeId().equals(order.getEventType().getId())) {
            EventType eventType = eventTypeRepository.findById(formDTO.getEventTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("EventType", "id", formDTO.getEventTypeId()));
            order.setEventType(eventType);
        }

        // Update event details
        order.setEventDate(formDTO.getEventDate());
        order.setEventTime(formDTO.getEventTime());
        order.setVenueName(formDTO.getVenueName());
        order.setVenueAddress(formDTO.getVenueAddress());
        order.setGuestCount(formDTO.getGuestCount());
        order.setNotes(formDTO.getNotes());

        // Update pricing
        formDTO.recalculateTotals();
        order.setDiscountPercent(formDTO.getDiscountPercent());
        order.setTaxPercent(formDTO.getTaxPercent());

        // Clear and rebuild menu items
        order.clearMenuItems();
        for (OrderMenuItemFormDTO menuItemDTO : formDTO.getMenuItems()) {
            Menu menu = menuRepository.findById(menuItemDTO.getMenuId())
                    .orElseThrow(() -> new ResourceNotFoundException("Menu", "id", menuItemDTO.getMenuId()));

            OrderMenuItem menuItem = OrderMenuItem.builder()
                    .menu(menu)
                    .quantity(menuItemDTO.getQuantity())
                    .pricePerItem(menuItemDTO.getPricePerItem())
                    .subtotal(menuItemDTO.getSubtotal())
                    .build();

            order.addMenuItem(menuItem);
        }

        // Clear and rebuild utilities
        order.clearUtilities();
        for (OrderUtilityFormDTO utilityDTO : formDTO.getUtilities()) {
            Utility utility = utilityRepository.findById(utilityDTO.getUtilityId())
                    .orElseThrow(() -> new ResourceNotFoundException("Utility", "id", utilityDTO.getUtilityId()));

            OrderUtility orderUtility = OrderUtility.builder()
                    .utility(utility)
                    .quantity(utilityDTO.getQuantity())
                    .pricePerItem(utilityDTO.getPricePerUnit())
                    .subtotal(utilityDTO.getSubtotal())
                    .build();

            order.addUtility(orderUtility);
        }

        // Recalculate totals on entity
        order.recalculateTotals();

        order.setUpdatedBy(userId);
        Order saved = orderRepository.save(order);
        log.info("Order updated from wizard: {} (ID: {})", saved.getOrderNumber(), saved.getId());

        return orderMapper.toDto(saved);
    }

    // ===== Workflow Methods =====

    @Override
    @Transactional
    public OrderDTO submit(Long orderId, Long userId) {
        log.debug("Submitting order {} for approval", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        order.submit(userId);
        order.setUpdatedBy(userId);

        Order saved = orderRepository.save(order);
        log.info("Order {} submitted for approval by user {}", saved.getOrderNumber(), userId);

        return orderMapper.toDto(saved);
    }

    @Override
    @Transactional
    public OrderDTO approve(Long orderId, Long userId) {
        log.debug("Approving order {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        order.approve(userId);
        order.setUpdatedBy(userId);

        Order saved = orderRepository.save(order);
        log.info("Order {} approved by user {}", saved.getOrderNumber(), userId);

        return orderMapper.toDto(saved);
    }

    @Override
    @Transactional
    public OrderDTO reject(Long orderId, Long userId, String reason) {
        log.debug("Rejecting order {} with reason: {}", orderId, reason);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Only PENDING orders can be rejected");
        }

        // Rejection returns order to DRAFT status
        order.setStatus(OrderStatus.DRAFT);
        order.setNotes(order.getNotes() != null
                ? order.getNotes() + "\n\nRejection reason: " + reason
                : "Rejection reason: " + reason);
        order.setUpdatedBy(userId);

        Order saved = orderRepository.save(order);
        log.info("Order {} rejected by user {}", saved.getOrderNumber(), userId);

        return orderMapper.toDto(saved);
    }

    @Override
    @Transactional
    public OrderDTO cancel(Long orderId, Long userId, String reason) {
        log.debug("Cancelling order {} with reason: {}", orderId, reason);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        order.cancel(userId, reason);
        order.setUpdatedBy(userId);

        Order saved = orderRepository.save(order);
        log.info("Order {} cancelled by user {}", saved.getOrderNumber(), userId);

        return orderMapper.toDto(saved);
    }

    @Override
    @Transactional
    public OrderDTO startProgress(Long orderId, Long userId) {
        log.debug("Starting progress on order {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        order.startProgress(userId);
        order.setUpdatedBy(userId);

        Order saved = orderRepository.save(order);
        log.info("Order {} started progress by user {}", saved.getOrderNumber(), userId);

        return orderMapper.toDto(saved);
    }

    @Override
    @Transactional
    public OrderDTO complete(Long orderId, Long userId) {
        log.debug("Completing order {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        order.complete(userId);
        order.setUpdatedBy(userId);

        Order saved = orderRepository.save(order);
        log.info("Order {} completed by user {}", saved.getOrderNumber(), userId);

        return orderMapper.toDto(saved);
    }

    @Override
    @Transactional
    public OrderDTO updateStatus(Long orderId, OrderStatus newStatus, Long userId, String notes) {
        log.debug("Updating order {} status to {}", orderId, newStatus);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        // Validate transition
        OrderStatus currentStatus = order.getStatus();
        if (!isValidTransition(currentStatus, newStatus)) {
            throw new IllegalStateException(
                    "Invalid status transition from " + currentStatus + " to " + newStatus);
        }

        // Apply appropriate workflow method based on new status
        switch (newStatus) {
            case PENDING -> order.submit(userId);
            case CONFIRMED -> order.approve(userId);
            case IN_PROGRESS -> order.startProgress(userId);
            case COMPLETED -> order.complete(userId);
            case CANCELLED -> order.cancel(userId, notes != null ? notes : "Status changed to cancelled");
            default -> order.setStatus(newStatus);
        }

        if (notes != null && !notes.isBlank()) {
            order.setNotes(order.getNotes() != null
                    ? order.getNotes() + "\n\n" + notes
                    : notes);
        }

        order.setUpdatedBy(userId);
        Order saved = orderRepository.save(order);
        log.info("Order {} status updated to {} by user {}", saved.getOrderNumber(), newStatus, userId);

        return orderMapper.toDto(saved);
    }

    /**
     * Validates if a status transition is allowed.
     */
    private boolean isValidTransition(OrderStatus from, OrderStatus to) {
        OrderStatus[] validNextStatuses = from.getNextStatuses();
        for (OrderStatus status : validNextStatuses) {
            if (status == to) {
                return true;
            }
        }
        return false;
    }

    // ===== Clone =====

    @Override
    @Transactional
    public OrderDTO cloneOrder(Long orderId, LocalDate newEventDate, Long userId) {
        log.debug("Cloning order {} with new event date {}", orderId, newEventDate);

        Order original = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        // Generate new order number
        String orderNumber = orderNumberGeneratorService.generateOrderNumber(original.getTenant().getId());

        // Create cloned order
        Order cloned = Order.builder()
                .tenant(original.getTenant())
                .orderNumber(orderNumber)
                .customer(original.getCustomer())
                .eventType(original.getEventType())
                .eventDate(newEventDate)
                .eventTime(original.getEventTime())
                .venueName(original.getVenueName())
                .venueAddress(original.getVenueAddress())
                .guestCount(original.getGuestCount())
                .discountPercent(original.getDiscountPercent())
                .taxPercent(original.getTaxPercent())
                .status(OrderStatus.DRAFT)
                .notes("Cloned from order " + original.getOrderNumber())
                .build();

        cloned.setCreatedBy(userId);

        // Clone menu items
        for (OrderMenuItem originalItem : original.getMenuItems()) {
            OrderMenuItem clonedItem = OrderMenuItem.builder()
                    .menu(originalItem.getMenu())
                    .quantity(originalItem.getQuantity())
                    .pricePerItem(originalItem.getPricePerItem())
                    .subtotal(originalItem.getSubtotal())
                    .build();
            cloned.addMenuItem(clonedItem);
        }

        // Clone utilities
        for (OrderUtility originalUtility : original.getUtilities()) {
            OrderUtility clonedUtility = OrderUtility.builder()
                    .utility(originalUtility.getUtility())
                    .quantity(originalUtility.getQuantity())
                    .pricePerItem(originalUtility.getPricePerItem())
                    .subtotal(originalUtility.getSubtotal())
                    .build();
            cloned.addUtility(clonedUtility);
        }

        // Recalculate totals
        cloned.recalculateTotals();

        Order saved = orderRepository.save(cloned);
        log.info("Order {} cloned as {} by user {}", original.getOrderNumber(), saved.getOrderNumber(), userId);

        return orderMapper.toDto(saved);
    }

    // ===== Dashboard Queries =====

    @Override
    @Transactional(readOnly = true)
    public long countByStatus(Long tenantId, OrderStatus status) {
        return orderRepository.countByTenantIdAndStatus(tenantId, status);
    }

    @Override
    @Transactional(readOnly = true)
    public long countTodaysOrders(Long tenantId) {
        return orderRepository.countTodaysOrders(tenantId, LocalDate.now());
    }

    @Override
    @Transactional(readOnly = true)
    public long countPendingApproval(Long tenantId) {
        return orderRepository.countPendingApproval(tenantId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDTO> getUpcomingOrders(Long tenantId, int days) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(days);
        List<Order> orders = orderRepository.findByEventDateRange(tenantId, today, endDate);
        return orderMapper.toDto(orders);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDTO> getRecentOrders(Long tenantId, int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Order> ordersPage = orderRepository.findByTenantId(tenantId, pageable);
        return orderMapper.toDto(ordersPage.getContent());
    }
}
