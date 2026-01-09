package com.smtech.SM_Caterer.fixtures;

import com.smtech.SM_Caterer.domain.entity.*;
import com.smtech.SM_Caterer.domain.enums.*;
import com.smtech.SM_Caterer.service.dto.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Factory class for creating test data.
 */
public class TestDataFactory {

    private static final AtomicLong ID_GENERATOR = new AtomicLong(1L);

    // ========================================
    // TENANT
    // ========================================

    public static Tenant createTenant() {
        long id = ID_GENERATOR.getAndIncrement();
        return Tenant.builder()
                .id(id)
                .tenantCode("TENANT_" + id)
                .businessName("Test Caterer " + id)
                .contactPerson("Contact Person")
                .email("tenant" + id + "@test.com")
                .phone("9876543210")
                .address("123 Test Street")
                .city("Test City")
                .state("Test State")
                .pincode("400001")
                .status(TenantStatus.ACTIVE)
                .paymentEnabled(true)
                .emailEnabled(false)
                .version(0L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static TenantDTO createTenantDTO() {
        long id = ID_GENERATOR.getAndIncrement();
        return TenantDTO.builder()
                .tenantCode("TENANT_" + id)
                .businessName("Test Caterer " + id)
                .contactPerson("Contact Person")
                .email("tenant" + id + "@test.com")
                .phone("9876543210")
                .status(TenantStatus.ACTIVE)
                .build();
    }

    // ========================================
    // CUSTOMER
    // ========================================

    public static Customer createCustomer(Tenant tenant) {
        long id = ID_GENERATOR.getAndIncrement();
        return Customer.builder()
                .id(id)
                .tenant(tenant)
                .customerCode("CUST_" + id)
                .name("Test Customer " + id)
                .email("customer" + id + "@test.com")
                .phone("9876543210")
                .address("456 Customer Street")
                .city("Customer City")
                .state("Customer State")
                .pincode("400002")
                .status(Status.ACTIVE)
                .version(0L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static CustomerDTO createCustomerDTO(Long tenantId) {
        long id = ID_GENERATOR.getAndIncrement();
        return CustomerDTO.builder()
                .tenantId(tenantId)
                .customerCode("CUST_" + id)
                .name("Test Customer " + id)
                .email("customer" + id + "@test.com")
                .phone("9876543210")
                .status(Status.ACTIVE)
                .build();
    }

    // ========================================
    // USER
    // ========================================

    public static User createUser(Tenant tenant) {
        long id = ID_GENERATOR.getAndIncrement();
        return User.builder()
                .id(id)
                .tenant(tenant)
                .username("user" + id)
                .password("$2a$10$N9qo8uLOickgx2ZMRZoMy.GqE3.kXw.OW2z/BbJ0p6K0JQk3xN4W.")
                .email("user" + id + "@test.com")
                .firstName("Test")
                .lastName("User " + id)
                .role(UserRole.TENANT_ADMIN)
                .status(UserStatus.ACTIVE)
                .failedLoginAttempts(0)
                .version(0L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static UserDTO createUserDTO(Long tenantId) {
        long id = ID_GENERATOR.getAndIncrement();
        return UserDTO.builder()
                .tenantId(tenantId)
                .username("user" + id)
                .email("user" + id + "@test.com")
                .firstName("Test")
                .lastName("User " + id)
                .role(UserRole.TENANT_ADMIN)
                .status(UserStatus.ACTIVE)
                .build();
    }

    // ========================================
    // EVENT TYPE
    // ========================================

    public static EventType createEventType(Tenant tenant) {
        long id = ID_GENERATOR.getAndIncrement();
        return EventType.builder()
                .id(id)
                .tenant(tenant)
                .eventCode("EVT_" + id)
                .status(Status.ACTIVE)
                .version(0L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static EventTypeDTO createEventTypeDTO(Long tenantId) {
        long id = ID_GENERATOR.getAndIncrement();
        return EventTypeDTO.builder()
                .tenantId(tenantId)
                .eventTypeCode("EVT_" + id)
                .status(Status.ACTIVE)
                .build();
    }

    // ========================================
    // MATERIAL GROUP
    // ========================================

    public static MaterialGroup createMaterialGroup(Tenant tenant) {
        long id = ID_GENERATOR.getAndIncrement();
        return MaterialGroup.builder()
                .id(id)
                .tenant(tenant)
                .groupCode("MG_" + id)
                .status(Status.ACTIVE)
                .version(0L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static MaterialGroupDTO createMaterialGroupDTO(Long tenantId) {
        long id = ID_GENERATOR.getAndIncrement();
        return MaterialGroupDTO.builder()
                .tenantId(tenantId)
                .groupCode("MG_" + id)
                .status(Status.ACTIVE)
                .build();
    }

    // ========================================
    // UNIT
    // ========================================

    public static Unit createUnit(Tenant tenant) {
        long id = ID_GENERATOR.getAndIncrement();
        return Unit.builder()
                .id(id)
                .tenant(tenant)
                .unitCode("UNIT_" + id)
                .status(Status.ACTIVE)
                .version(0L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static UnitDTO createUnitDTO(Long tenantId) {
        long id = ID_GENERATOR.getAndIncrement();
        return UnitDTO.builder()
                .tenantId(tenantId)
                .unitCode("UNIT_" + id)
                .status(Status.ACTIVE)
                .build();
    }

    // ========================================
    // MATERIAL
    // ========================================

    public static Material createMaterial(Tenant tenant, MaterialGroup group, Unit unit) {
        long id = ID_GENERATOR.getAndIncrement();
        return Material.builder()
                .id(id)
                .tenant(tenant)
                .materialGroup(group)
                .unit(unit)
                .materialCode("MAT_" + id)
                .costPerUnit(new BigDecimal("10.00"))
                .minimumStock(new BigDecimal("5.00"))
                .currentStock(new BigDecimal("20.00"))
                .status(Status.ACTIVE)
                .version(0L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static MaterialDTO createMaterialDTO(Long tenantId, Long groupId, Long unitId) {
        long id = ID_GENERATOR.getAndIncrement();
        return MaterialDTO.builder()
                .tenantId(tenantId)
                .materialGroupId(groupId)
                .unitId(unitId)
                .materialCode("MAT_" + id)
                .costPerUnit(new BigDecimal("10.00"))
                .status(Status.ACTIVE)
                .build();
    }

    // ========================================
    // MENU
    // ========================================

    public static Menu createMenu(Tenant tenant) {
        long id = ID_GENERATOR.getAndIncrement();
        return Menu.builder()
                .id(id)
                .tenant(tenant)
                .menuCode("MENU_" + id)
                .category(MenuCategory.VEG)
                .costPerServe(new BigDecimal("100.00"))
                .servesCount(1)
                .status(Status.ACTIVE)
                .version(0L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static MenuDTO createMenuDTO(Long tenantId) {
        long id = ID_GENERATOR.getAndIncrement();
        return MenuDTO.builder()
                .tenantId(tenantId)
                .menuCode("MENU_" + id)
                .category(MenuCategory.VEG)
                .costPerServe(new BigDecimal("100.00"))
                .servesCount(1)
                .status(Status.ACTIVE)
                .build();
    }

    // ========================================
    // UTILITY
    // ========================================

    public static Utility createUtility(Tenant tenant) {
        long id = ID_GENERATOR.getAndIncrement();
        return Utility.builder()
                .id(id)
                .tenant(tenant)
                .utilityCode("UTIL_" + id)
                .costPerUnit(new BigDecimal("50.00"))
                .status(Status.ACTIVE)
                .version(0L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static UtilityDTO createUtilityDTO(Long tenantId) {
        long id = ID_GENERATOR.getAndIncrement();
        return UtilityDTO.builder()
                .tenantId(tenantId)
                .utilityCode("UTIL_" + id)
                .price(new BigDecimal("50.00"))
                .status(Status.ACTIVE)
                .build();
    }

    // ========================================
    // ORDER
    // ========================================

    public static Order createOrder(Tenant tenant, Customer customer, EventType eventType) {
        long id = ID_GENERATOR.getAndIncrement();
        return Order.builder()
                .id(id)
                .tenant(tenant)
                .customer(customer)
                .eventType(eventType)
                .orderNumber("ORD-" + String.format("%06d", id))
                .eventDate(LocalDate.now().plusDays(7))
                .eventTime(LocalTime.of(12, 0))
                .venueName("Test Venue")
                .venueAddress("Test Venue Address")
                .guestCount(100)
                .menuSubtotal(new BigDecimal("50000.00"))
                .utilitySubtotal(new BigDecimal("5000.00"))
                .totalAmount(new BigDecimal("55000.00"))
                .grandTotal(new BigDecimal("55000.00"))
                .advanceAmount(new BigDecimal("20000.00"))
                .balanceAmount(new BigDecimal("35000.00"))
                .status(OrderStatus.CONFIRMED)
                .version(0L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static OrderDTO createOrderDTO(Long tenantId, Long customerId, Long eventTypeId) {
        long id = ID_GENERATOR.getAndIncrement();
        return OrderDTO.builder()
                .tenantId(tenantId)
                .customerId(customerId)
                .eventTypeId(eventTypeId)
                .orderNumber("ORD-" + String.format("%06d", id))
                .eventDate(LocalDate.now().plusDays(7))
                .eventTime(LocalTime.of(12, 0))
                .venueName("Test Venue")
                .guestCount(100)
                .status(OrderStatus.DRAFT)
                .build();
    }

    // ========================================
    // PAYMENT
    // ========================================

    public static Payment createPayment(Tenant tenant, Order order) {
        long id = ID_GENERATOR.getAndIncrement();
        return Payment.builder()
                .id(id)
                .tenant(tenant)
                .order(order)
                .paymentNumber("PAY-" + String.format("%06d", id))
                .amount(new BigDecimal("10000.00"))
                .paymentMethod(PaymentMethod.CASH)
                .paymentDate(LocalDate.now())
                .status(PaymentStatus.COMPLETED)
                .version(0L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static PaymentDTO createPaymentDTO(Long tenantId, Long orderId) {
        return PaymentDTO.builder()
                .tenantId(tenantId)
                .orderId(orderId)
                .amount(new BigDecimal("10000.00"))
                .paymentMethod(PaymentMethod.CASH)
                .paymentDate(LocalDate.now())
                .status(PaymentStatus.COMPLETED)
                .build();
    }

    // ========================================
    // UTILITY METHODS
    // ========================================

    public static void resetIdGenerators() {
        ID_GENERATOR.set(1L);
    }

    public static Long nextId() {
        return ID_GENERATOR.getAndIncrement();
    }
}
