package com.smtech.SM_Caterer.domain;

import com.smtech.SM_Caterer.base.BaseUnitTest;
import com.smtech.SM_Caterer.domain.entity.*;
import com.smtech.SM_Caterer.domain.enums.*;
import com.smtech.SM_Caterer.fixtures.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Comprehensive entity validation tests.
 * Tests entity construction, field mapping, business methods, and workflow transitions.
 */
@DisplayName("Entity Validation Tests")
class EntityValidationTest extends BaseUnitTest {

    private Tenant tenant;
    private Customer customer;
    private EventType eventType;

    @BeforeEach
    void setUpTestData() {
        TestDataFactory.resetIdGenerators();
        tenant = TestDataFactory.createTenant();
        customer = TestDataFactory.createCustomer(tenant);
        eventType = TestDataFactory.createEventType(tenant);
    }

    // ========================================================================
    // ORDER ENTITY
    // ========================================================================

    @Nested
    @DisplayName("Order Entity")
    class OrderEntityTests {

        @Nested
        @DisplayName("Builder and Field Mapping")
        class BuilderTests {

            @Test
            @DisplayName("Should create valid order via builder")
            void shouldCreateValidOrder() {
                Order order = TestDataFactory.createOrder(tenant, customer, eventType);

                assertThat(order).isNotNull();
                assertThat(order.getId()).isNotNull();
                assertThat(order.getOrderNumber()).startsWith("ORD-");
                assertThat(order.getTenant()).isEqualTo(tenant);
                assertThat(order.getCustomer()).isEqualTo(customer);
                assertThat(order.getEventType()).isEqualTo(eventType);
                assertThat(order.getEventDate()).isAfter(LocalDate.now());
                assertThat(order.getEventTime()).isNotNull();
                assertThat(order.getVenueName()).isEqualTo("Test Venue");
                assertThat(order.getVenueAddress()).isEqualTo("Test Venue Address");
                assertThat(order.getGuestCount()).isEqualTo(100);
            }

            @Test
            @DisplayName("Should have correct default status from factory (CONFIRMED)")
            void shouldHaveCorrectFactoryStatus() {
                Order order = TestDataFactory.createOrder(tenant, customer, eventType);
                assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
            }

            @Test
            @DisplayName("Should set financial fields correctly from factory")
            void shouldSetFinancialFieldsCorrectly() {
                Order order = TestDataFactory.createOrder(tenant, customer, eventType);

                assertThat(order.getMenuSubtotal()).isEqualByComparingTo(new BigDecimal("50000.00"));
                assertThat(order.getUtilitySubtotal()).isEqualByComparingTo(new BigDecimal("5000.00"));
                assertThat(order.getTotalAmount()).isEqualByComparingTo(new BigDecimal("55000.00"));
                assertThat(order.getGrandTotal()).isEqualByComparingTo(new BigDecimal("55000.00"));
                assertThat(order.getAdvanceAmount()).isEqualByComparingTo(new BigDecimal("20000.00"));
                assertThat(order.getBalanceAmount()).isEqualByComparingTo(new BigDecimal("35000.00"));
            }

            @Test
            @DisplayName("Grand total should equal advance plus balance")
            void grandTotalShouldEqualAdvancePlusBalance() {
                Order order = TestDataFactory.createOrder(tenant, customer, eventType);

                BigDecimal reconstructedGrandTotal = order.getAdvanceAmount().add(order.getBalanceAmount());
                assertThat(order.getGrandTotal()).isEqualByComparingTo(reconstructedGrandTotal);
            }
        }

        @Nested
        @DisplayName("isEditable() Method")
        class IsEditableTests {

            @Test
            @DisplayName("Should return true for DRAFT status")
            void shouldReturnTrueForDraft() {
                Order order = TestDataFactory.createOrder(tenant, customer, eventType);
                order.setStatus(OrderStatus.DRAFT);

                assertThat(order.isEditable()).isTrue();
            }

            @Test
            @DisplayName("Should return true for PENDING status")
            void shouldReturnTrueForPending() {
                Order order = TestDataFactory.createOrder(tenant, customer, eventType);
                order.setStatus(OrderStatus.PENDING);

                assertThat(order.isEditable()).isTrue();
            }

            @Test
            @DisplayName("Should return false for CONFIRMED status")
            void shouldReturnFalseForConfirmed() {
                Order order = TestDataFactory.createOrder(tenant, customer, eventType);
                order.setStatus(OrderStatus.CONFIRMED);

                assertThat(order.isEditable()).isFalse();
            }

            @Test
            @DisplayName("Should return false for IN_PROGRESS status")
            void shouldReturnFalseForInProgress() {
                Order order = TestDataFactory.createOrder(tenant, customer, eventType);
                order.setStatus(OrderStatus.IN_PROGRESS);

                assertThat(order.isEditable()).isFalse();
            }

            @Test
            @DisplayName("Should return false for COMPLETED status")
            void shouldReturnFalseForCompleted() {
                Order order = TestDataFactory.createOrder(tenant, customer, eventType);
                order.setStatus(OrderStatus.COMPLETED);

                assertThat(order.isEditable()).isFalse();
            }

            @Test
            @DisplayName("Should return false for CANCELLED status")
            void shouldReturnFalseForCancelled() {
                Order order = TestDataFactory.createOrder(tenant, customer, eventType);
                order.setStatus(OrderStatus.CANCELLED);

                assertThat(order.isEditable()).isFalse();
            }

            @Test
            @DisplayName("Should return false when status is null")
            void shouldReturnFalseWhenStatusIsNull() {
                Order order = TestDataFactory.createOrder(tenant, customer, eventType);
                order.setStatus(null);

                assertThat(order.isEditable()).isFalse();
            }
        }

        @Nested
        @DisplayName("isCancellable() Method")
        class IsCancellableTests {

            @Test
            @DisplayName("Should return true for DRAFT status")
            void shouldReturnTrueForDraft() {
                Order order = TestDataFactory.createOrder(tenant, customer, eventType);
                order.setStatus(OrderStatus.DRAFT);

                assertThat(order.isCancellable()).isTrue();
            }

            @Test
            @DisplayName("Should return true for PENDING status")
            void shouldReturnTrueForPending() {
                Order order = TestDataFactory.createOrder(tenant, customer, eventType);
                order.setStatus(OrderStatus.PENDING);

                assertThat(order.isCancellable()).isTrue();
            }

            @Test
            @DisplayName("Should return true for CONFIRMED status")
            void shouldReturnTrueForConfirmed() {
                Order order = TestDataFactory.createOrder(tenant, customer, eventType);
                order.setStatus(OrderStatus.CONFIRMED);

                assertThat(order.isCancellable()).isTrue();
            }

            @Test
            @DisplayName("Should return true for IN_PROGRESS status")
            void shouldReturnTrueForInProgress() {
                Order order = TestDataFactory.createOrder(tenant, customer, eventType);
                order.setStatus(OrderStatus.IN_PROGRESS);

                assertThat(order.isCancellable()).isTrue();
            }

            @Test
            @DisplayName("Should return false for COMPLETED status")
            void shouldReturnFalseForCompleted() {
                Order order = TestDataFactory.createOrder(tenant, customer, eventType);
                order.setStatus(OrderStatus.COMPLETED);

                assertThat(order.isCancellable()).isFalse();
            }

            @Test
            @DisplayName("Should return false for CANCELLED status")
            void shouldReturnFalseForCancelled() {
                Order order = TestDataFactory.createOrder(tenant, customer, eventType);
                order.setStatus(OrderStatus.CANCELLED);

                assertThat(order.isCancellable()).isFalse();
            }

            @Test
            @DisplayName("Should return false when status is null")
            void shouldReturnFalseWhenStatusIsNull() {
                Order order = TestDataFactory.createOrder(tenant, customer, eventType);
                order.setStatus(null);

                assertThat(order.isCancellable()).isFalse();
            }
        }

        @Nested
        @DisplayName("Order Status Workflow")
        class WorkflowTests {

            @Test
            @DisplayName("submit() should transition DRAFT to PENDING")
            void submitShouldTransitionDraftToPending() {
                Order order = TestDataFactory.createOrder(tenant, customer, eventType);
                order.setStatus(OrderStatus.DRAFT);

                order.submit(1L);

                assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
                assertThat(order.getSubmittedAt()).isNotNull();
                assertThat(order.getSubmittedBy()).isEqualTo(1L);
            }

            @Test
            @DisplayName("submit() should throw if not in DRAFT status")
            void submitShouldThrowIfNotDraft() {
                Order order = TestDataFactory.createOrder(tenant, customer, eventType);
                order.setStatus(OrderStatus.CONFIRMED);

                assertThatThrownBy(() -> order.submit(1L))
                        .isInstanceOf(IllegalStateException.class)
                        .hasMessageContaining("Only DRAFT orders can be submitted");
            }

            @Test
            @DisplayName("approve() should transition PENDING to CONFIRMED")
            void approveShouldTransitionPendingToConfirmed() {
                Order order = TestDataFactory.createOrder(tenant, customer, eventType);
                order.setStatus(OrderStatus.PENDING);

                order.approve(2L);

                assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
                assertThat(order.getApprovedAt()).isNotNull();
                assertThat(order.getApprovedBy()).isEqualTo(2L);
            }

            @Test
            @DisplayName("approve() should throw if not in PENDING status")
            void approveShouldThrowIfNotPending() {
                Order order = TestDataFactory.createOrder(tenant, customer, eventType);
                order.setStatus(OrderStatus.DRAFT);

                assertThatThrownBy(() -> order.approve(2L))
                        .isInstanceOf(IllegalStateException.class)
                        .hasMessageContaining("Only PENDING orders can be approved");
            }

            @Test
            @DisplayName("startProgress() should transition CONFIRMED to IN_PROGRESS")
            void startProgressShouldTransitionConfirmedToInProgress() {
                Order order = TestDataFactory.createOrder(tenant, customer, eventType);
                order.setStatus(OrderStatus.CONFIRMED);

                order.startProgress(3L);

                assertThat(order.getStatus()).isEqualTo(OrderStatus.IN_PROGRESS);
            }

            @Test
            @DisplayName("startProgress() should throw if not in CONFIRMED status")
            void startProgressShouldThrowIfNotConfirmed() {
                Order order = TestDataFactory.createOrder(tenant, customer, eventType);
                order.setStatus(OrderStatus.PENDING);

                assertThatThrownBy(() -> order.startProgress(3L))
                        .isInstanceOf(IllegalStateException.class)
                        .hasMessageContaining("Only CONFIRMED orders can be started");
            }

            @Test
            @DisplayName("complete() should transition IN_PROGRESS to COMPLETED")
            void completeShouldTransitionInProgressToCompleted() {
                Order order = TestDataFactory.createOrder(tenant, customer, eventType);
                order.setStatus(OrderStatus.IN_PROGRESS);

                order.complete(4L);

                assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);
                assertThat(order.getCompletedAt()).isNotNull();
                assertThat(order.getCompletedBy()).isEqualTo(4L);
            }

            @Test
            @DisplayName("complete() should throw if not in IN_PROGRESS status")
            void completeShouldThrowIfNotInProgress() {
                Order order = TestDataFactory.createOrder(tenant, customer, eventType);
                order.setStatus(OrderStatus.CONFIRMED);

                assertThatThrownBy(() -> order.complete(4L))
                        .isInstanceOf(IllegalStateException.class)
                        .hasMessageContaining("Only IN_PROGRESS orders can be completed");
            }

            @Test
            @DisplayName("cancel() should transition cancellable order to CANCELLED")
            void cancelShouldTransitionCancellableOrderToCancelled() {
                Order order = TestDataFactory.createOrder(tenant, customer, eventType);
                order.setStatus(OrderStatus.CONFIRMED);

                order.cancel(5L, "Customer requested cancellation");

                assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
                assertThat(order.getCancelledAt()).isNotNull();
                assertThat(order.getCancelledBy()).isEqualTo(5L);
                assertThat(order.getCancellationReason()).isEqualTo("Customer requested cancellation");
            }

            @Test
            @DisplayName("cancel() should throw if order is already COMPLETED")
            void cancelShouldThrowIfCompleted() {
                Order order = TestDataFactory.createOrder(tenant, customer, eventType);
                order.setStatus(OrderStatus.COMPLETED);

                assertThatThrownBy(() -> order.cancel(5L, "Too late"))
                        .isInstanceOf(IllegalStateException.class)
                        .hasMessageContaining("Order cannot be cancelled");
            }

            @Test
            @DisplayName("cancel() should throw if order is already CANCELLED")
            void cancelShouldThrowIfAlreadyCancelled() {
                Order order = TestDataFactory.createOrder(tenant, customer, eventType);
                order.setStatus(OrderStatus.CANCELLED);

                assertThatThrownBy(() -> order.cancel(5L, "Try again"))
                        .isInstanceOf(IllegalStateException.class)
                        .hasMessageContaining("Order cannot be cancelled");
            }

            @Test
            @DisplayName("cancel() should throw if reason is null")
            void cancelShouldThrowIfReasonIsNull() {
                Order order = TestDataFactory.createOrder(tenant, customer, eventType);
                order.setStatus(OrderStatus.DRAFT);

                assertThatThrownBy(() -> order.cancel(5L, null))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessageContaining("Cancellation reason is required");
            }

            @Test
            @DisplayName("cancel() should throw if reason is blank")
            void cancelShouldThrowIfReasonIsBlank() {
                Order order = TestDataFactory.createOrder(tenant, customer, eventType);
                order.setStatus(OrderStatus.DRAFT);

                assertThatThrownBy(() -> order.cancel(5L, "   "))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessageContaining("Cancellation reason is required");
            }

            @Test
            @DisplayName("Full lifecycle: DRAFT -> PENDING -> CONFIRMED -> IN_PROGRESS -> COMPLETED")
            void fullLifecycleShouldWorkCorrectly() {
                Order order = TestDataFactory.createOrder(tenant, customer, eventType);
                order.setStatus(OrderStatus.DRAFT);

                // Submit
                order.submit(1L);
                assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);

                // Approve
                order.approve(2L);
                assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);

                // Start
                order.startProgress(3L);
                assertThat(order.getStatus()).isEqualTo(OrderStatus.IN_PROGRESS);

                // Complete
                order.complete(4L);
                assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);
            }
        }

        @Nested
        @DisplayName("Financial Calculations")
        class FinancialTests {

            @Test
            @DisplayName("Balance should equal grandTotal minus advanceAmount")
            void balanceShouldEqualGrandTotalMinusAdvance() {
                Order order = TestDataFactory.createOrder(tenant, customer, eventType);

                BigDecimal expectedBalance = order.getGrandTotal().subtract(order.getAdvanceAmount());
                assertThat(order.getBalanceAmount()).isEqualByComparingTo(expectedBalance);
            }

            @Test
            @DisplayName("Total amount should equal menu subtotal plus utility subtotal")
            void totalAmountShouldEqualSubtotals() {
                Order order = TestDataFactory.createOrder(tenant, customer, eventType);

                BigDecimal expectedTotal = order.getMenuSubtotal().add(order.getUtilitySubtotal());
                assertThat(order.getTotalAmount()).isEqualByComparingTo(expectedTotal);
            }

            @Test
            @DisplayName("isFullyPaid() should return true when balance is zero")
            void isFullyPaidShouldReturnTrueWhenBalanceIsZero() {
                Order order = TestDataFactory.createOrder(tenant, customer, eventType);
                order.setBalanceAmount(BigDecimal.ZERO);

                assertThat(order.isFullyPaid()).isTrue();
            }

            @Test
            @DisplayName("isFullyPaid() should return false when balance is positive")
            void isFullyPaidShouldReturnFalseWhenBalanceIsPositive() {
                Order order = TestDataFactory.createOrder(tenant, customer, eventType);
                order.setBalanceAmount(new BigDecimal("10000.00"));

                assertThat(order.isFullyPaid()).isFalse();
            }

            @Test
            @DisplayName("recalculateBalance() should update advance and balance from payments")
            void recalculateBalanceShouldUpdateFromPayments() {
                Order order = TestDataFactory.createOrder(tenant, customer, eventType);
                order.setGrandTotal(new BigDecimal("50000.00"));

                Payment payment1 = TestDataFactory.createPayment(tenant, order);
                payment1.setAmount(new BigDecimal("15000.00"));
                Payment payment2 = TestDataFactory.createPayment(tenant, order);
                payment2.setAmount(new BigDecimal("10000.00"));

                order.getPayments().clear();
                order.addPayment(payment1);
                order.addPayment(payment2);

                order.recalculateBalance();

                assertThat(order.getAdvanceAmount()).isEqualByComparingTo(new BigDecimal("25000.00"));
                assertThat(order.getBalanceAmount()).isEqualByComparingTo(new BigDecimal("25000.00"));
            }

            @Test
            @DisplayName("recalculateBalance() should set balance to zero if overpaid")
            void recalculateBalanceShouldNotGoNegative() {
                Order order = TestDataFactory.createOrder(tenant, customer, eventType);
                order.setGrandTotal(new BigDecimal("10000.00"));

                Payment payment = TestDataFactory.createPayment(tenant, order);
                payment.setAmount(new BigDecimal("15000.00"));

                order.getPayments().clear();
                order.addPayment(payment);

                order.recalculateBalance();

                assertThat(order.getBalanceAmount()).isEqualByComparingTo(BigDecimal.ZERO);
            }
        }

        @Nested
        @DisplayName("Collection Helper Methods")
        class CollectionTests {

            @Test
            @DisplayName("addMenuItem() should add item and set back-reference")
            void addMenuItemShouldSetBackReference() {
                Order order = TestDataFactory.createOrder(tenant, customer, eventType);
                OrderMenuItem menuItem = new OrderMenuItem();

                order.addMenuItem(menuItem);

                assertThat(order.getMenuItems()).contains(menuItem);
                assertThat(menuItem.getOrder()).isEqualTo(order);
            }

            @Test
            @DisplayName("removeMenuItem() should remove item and clear back-reference")
            void removeMenuItemShouldClearBackReference() {
                Order order = TestDataFactory.createOrder(tenant, customer, eventType);
                OrderMenuItem menuItem = new OrderMenuItem();
                order.addMenuItem(menuItem);

                order.removeMenuItem(menuItem);

                assertThat(order.getMenuItems()).doesNotContain(menuItem);
                assertThat(menuItem.getOrder()).isNull();
            }

            @Test
            @DisplayName("clearMenuItems() should remove all items")
            void clearMenuItemsShouldRemoveAll() {
                Order order = TestDataFactory.createOrder(tenant, customer, eventType);
                OrderMenuItem item1 = new OrderMenuItem();
                OrderMenuItem item2 = new OrderMenuItem();
                order.addMenuItem(item1);
                order.addMenuItem(item2);

                order.clearMenuItems();

                assertThat(order.getMenuItems()).isEmpty();
                assertThat(item1.getOrder()).isNull();
                assertThat(item2.getOrder()).isNull();
            }

            @Test
            @DisplayName("addUtility() should add utility and set back-reference")
            void addUtilityShouldSetBackReference() {
                Order order = TestDataFactory.createOrder(tenant, customer, eventType);
                OrderUtility utility = new OrderUtility();

                order.addUtility(utility);

                assertThat(order.getUtilities()).contains(utility);
                assertThat(utility.getOrder()).isEqualTo(order);
            }

            @Test
            @DisplayName("addPayment() should add payment and set back-reference")
            void addPaymentShouldSetBackReference() {
                Order order = TestDataFactory.createOrder(tenant, customer, eventType);
                Payment payment = TestDataFactory.createPayment(tenant, order);

                order.getPayments().clear();
                order.addPayment(payment);

                assertThat(order.getPayments()).contains(payment);
                assertThat(payment.getOrder()).isEqualTo(order);
            }
        }
    }

    // ========================================================================
    // TENANT ENTITY
    // ========================================================================

    @Nested
    @DisplayName("Tenant Entity")
    class TenantEntityTests {

        @Test
        @DisplayName("Should create valid tenant via factory")
        void shouldCreateValidTenant() {
            assertThat(tenant).isNotNull();
            assertThat(tenant.getId()).isNotNull();
            assertThat(tenant.getTenantCode()).startsWith("TENANT_");
            assertThat(tenant.getBusinessName()).startsWith("Test Caterer");
            assertThat(tenant.getContactPerson()).isEqualTo("Contact Person");
            assertThat(tenant.getEmail()).contains("@test.com");
            assertThat(tenant.getPhone()).isEqualTo("9876543210");
            assertThat(tenant.getAddress()).isEqualTo("123 Test Street");
            assertThat(tenant.getCity()).isEqualTo("Test City");
            assertThat(tenant.getState()).isEqualTo("Test State");
            assertThat(tenant.getPincode()).isEqualTo("400001");
        }

        @Test
        @DisplayName("Tenant with ACTIVE status should be active")
        void tenantWithActiveStatusShouldBeActive() {
            assertThat(tenant.getStatus()).isEqualTo(TenantStatus.ACTIVE);
        }

        @Test
        @DisplayName("Tenant code should be set correctly")
        void tenantCodeShouldBeSetCorrectly() {
            assertThat(tenant.getTenantCode()).isNotBlank();
            assertThat(tenant.getTenantCode()).matches("^TENANT_\\d+$");
        }

        @Test
        @DisplayName("Should support INACTIVE status")
        void shouldSupportInactiveStatus() {
            tenant.setStatus(TenantStatus.INACTIVE);
            assertThat(tenant.getStatus()).isEqualTo(TenantStatus.INACTIVE);
        }

        @Test
        @DisplayName("Should support SUSPENDED status")
        void shouldSupportSuspendedStatus() {
            tenant.setStatus(TenantStatus.SUSPENDED);
            assertThat(tenant.getStatus()).isEqualTo(TenantStatus.SUSPENDED);
        }

        @Test
        @DisplayName("paymentEnabled should default to true from factory")
        void paymentEnabledShouldDefaultToTrue() {
            assertThat(tenant.getPaymentEnabled()).isTrue();
        }

        @Test
        @DisplayName("isPaymentFeatureEnabled() should return correct value")
        void isPaymentFeatureEnabledShouldReturnCorrectValue() {
            tenant.setPaymentEnabled(true);
            assertThat(tenant.isPaymentFeatureEnabled()).isTrue();

            tenant.setPaymentEnabled(false);
            assertThat(tenant.isPaymentFeatureEnabled()).isFalse();

            tenant.setPaymentEnabled(null);
            assertThat(tenant.isPaymentFeatureEnabled()).isFalse();
        }

        @Test
        @DisplayName("getEffectiveDisplayName() should fall back to business name")
        void effectiveDisplayNameShouldFallBackToBusinessName() {
            tenant.setDisplayName(null);
            assertThat(tenant.getEffectiveDisplayName()).isEqualTo(tenant.getBusinessName());

            tenant.setDisplayName("  ");
            assertThat(tenant.getEffectiveDisplayName()).isEqualTo(tenant.getBusinessName());

            tenant.setDisplayName("My Catering Co.");
            assertThat(tenant.getEffectiveDisplayName()).isEqualTo("My Catering Co.");
        }

        @Test
        @DisplayName("getEffectivePrimaryColor() should fall back to default")
        void effectivePrimaryColorShouldFallBackToDefault() {
            tenant.setPrimaryColor(null);
            assertThat(tenant.getEffectivePrimaryColor()).isEqualTo("#3498db");

            tenant.setPrimaryColor("#ff5733");
            assertThat(tenant.getEffectivePrimaryColor()).isEqualTo("#ff5733");
        }

        @Test
        @DisplayName("isSubscriptionActive() should return true when no end date is set")
        void subscriptionShouldBeActiveWithNoEndDate() {
            tenant.setSubscriptionEndDate(null);
            assertThat(tenant.isSubscriptionActive()).isTrue();
        }

        @Test
        @DisplayName("isSubscriptionActive() should return true when end date is in the future")
        void subscriptionShouldBeActiveWhenEndDateInFuture() {
            tenant.setSubscriptionEndDate(LocalDate.now().plusDays(30));
            assertThat(tenant.isSubscriptionActive()).isTrue();
        }

        @Test
        @DisplayName("isSubscriptionActive() should return false when end date has passed")
        void subscriptionShouldBeInactiveWhenEndDatePassed() {
            tenant.setSubscriptionEndDate(LocalDate.now().minusDays(1));
            assertThat(tenant.isSubscriptionActive()).isFalse();
        }

        @Test
        @DisplayName("hasLogo() should correctly detect logo configuration")
        void hasLogoShouldDetectLogoConfiguration() {
            tenant.setLogoPath(null);
            assertThat(tenant.hasLogo()).isFalse();

            tenant.setLogoPath("  ");
            assertThat(tenant.hasLogo()).isFalse();

            tenant.setLogoPath("/images/logo.png");
            assertThat(tenant.hasLogo()).isTrue();
        }

        @Test
        @DisplayName("isEmailConfigured() should validate email configuration")
        void isEmailConfiguredShouldValidateConfiguration() {
            // Not configured by default from factory
            assertThat(tenant.isEmailConfigured()).isFalse();

            // Fully configured
            tenant.setEmailEnabled(true);
            tenant.setSmtpHost("smtp.example.com");
            tenant.setSmtpPort(587);
            tenant.setSmtpFromEmail("noreply@example.com");
            assertThat(tenant.isEmailConfigured()).isTrue();

            // Missing host
            tenant.setSmtpHost(null);
            assertThat(tenant.isEmailConfigured()).isFalse();
        }
    }

    // ========================================================================
    // CUSTOMER ENTITY
    // ========================================================================

    @Nested
    @DisplayName("Customer Entity")
    class CustomerEntityTests {

        @Test
        @DisplayName("Should create valid customer via factory")
        void shouldCreateValidCustomer() {
            assertThat(customer).isNotNull();
            assertThat(customer.getId()).isNotNull();
            assertThat(customer.getCustomerCode()).startsWith("CUST_");
            assertThat(customer.getName()).startsWith("Test Customer");
            assertThat(customer.getEmail()).contains("@test.com");
            assertThat(customer.getPhone()).isEqualTo("9876543210");
            assertThat(customer.getAddress()).isEqualTo("456 Customer Street");
            assertThat(customer.getCity()).isEqualTo("Customer City");
            assertThat(customer.getState()).isEqualTo("Customer State");
            assertThat(customer.getPincode()).isEqualTo("400002");
        }

        @Test
        @DisplayName("Customer should be associated with a Tenant")
        void customerShouldBeAssociatedWithTenant() {
            assertThat(customer.getTenant()).isNotNull();
            assertThat(customer.getTenant()).isEqualTo(tenant);
        }

        @Test
        @DisplayName("Customer status should default to ACTIVE from factory")
        void customerStatusShouldDefaultToActive() {
            assertThat(customer.getStatus()).isEqualTo(Status.ACTIVE);
        }

        @Test
        @DisplayName("Customer should support INACTIVE status")
        void customerShouldSupportInactiveStatus() {
            customer.setStatus(Status.INACTIVE);
            assertThat(customer.getStatus()).isEqualTo(Status.INACTIVE);
        }

        @Test
        @DisplayName("Customer code should be unique per tenant")
        void customerCodeShouldBeSet() {
            assertThat(customer.getCustomerCode()).isNotBlank();
            assertThat(customer.getCustomerCode()).matches("^CUST_\\d+$");
        }

        @Test
        @DisplayName("Customer builder should create entity with all fields populated")
        void builderShouldCreateEntityWithAllFields() {
            Customer newCustomer = Customer.builder()
                    .id(99L)
                    .tenant(tenant)
                    .customerCode("CUST_99")
                    .name("Custom Customer")
                    .email("custom@test.com")
                    .phone("1234567890")
                    .address("Custom Address")
                    .city("Custom City")
                    .state("Custom State")
                    .pincode("500001")
                    .gstin("22AAAAA0000A1Z5")
                    .status(Status.ACTIVE)
                    .build();

            assertThat(newCustomer.getId()).isEqualTo(99L);
            assertThat(newCustomer.getTenant()).isEqualTo(tenant);
            assertThat(newCustomer.getCustomerCode()).isEqualTo("CUST_99");
            assertThat(newCustomer.getName()).isEqualTo("Custom Customer");
            assertThat(newCustomer.getEmail()).isEqualTo("custom@test.com");
            assertThat(newCustomer.getPhone()).isEqualTo("1234567890");
            assertThat(newCustomer.getAddress()).isEqualTo("Custom Address");
            assertThat(newCustomer.getCity()).isEqualTo("Custom City");
            assertThat(newCustomer.getState()).isEqualTo("Custom State");
            assertThat(newCustomer.getPincode()).isEqualTo("500001");
            assertThat(newCustomer.getGstin()).isEqualTo("22AAAAA0000A1Z5");
            assertThat(newCustomer.getStatus()).isEqualTo(Status.ACTIVE);
        }
    }

    // ========================================================================
    // USER ENTITY
    // ========================================================================

    @Nested
    @DisplayName("User Entity")
    class UserEntityTests {

        private User user;

        @BeforeEach
        void setUp() {
            user = TestDataFactory.createUser(tenant);
        }

        @Test
        @DisplayName("Should create valid user via factory")
        void shouldCreateValidUser() {
            assertThat(user).isNotNull();
            assertThat(user.getId()).isNotNull();
            assertThat(user.getUsername()).startsWith("user");
            assertThat(user.getEmail()).contains("@test.com");
            assertThat(user.getFirstName()).isEqualTo("Test");
            assertThat(user.getLastName()).startsWith("User");
        }

        @Test
        @DisplayName("User role should be set correctly")
        void userRoleShouldBeSetCorrectly() {
            assertThat(user.getRole()).isEqualTo(UserRole.TENANT_ADMIN);

            user.setRole(UserRole.MANAGER);
            assertThat(user.getRole()).isEqualTo(UserRole.MANAGER);

            user.setRole(UserRole.STAFF);
            assertThat(user.getRole()).isEqualTo(UserRole.STAFF);

            user.setRole(UserRole.VIEWER);
            assertThat(user.getRole()).isEqualTo(UserRole.VIEWER);

            user.setRole(UserRole.SUPER_ADMIN);
            assertThat(user.getRole()).isEqualTo(UserRole.SUPER_ADMIN);
        }

        @Test
        @DisplayName("User should have a password field set")
        void userShouldHavePasswordField() {
            assertThat(user.getPassword()).isNotNull();
            assertThat(user.getPassword()).isNotBlank();
            // BCrypt hashes start with $2a$
            assertThat(user.getPassword()).startsWith("$2a$");
        }

        @Test
        @DisplayName("User status should default to ACTIVE from factory")
        void userStatusShouldDefaultToActive() {
            assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        }

        @Test
        @DisplayName("User should support all status values")
        void userShouldSupportAllStatusValues() {
            user.setStatus(UserStatus.ACTIVE);
            assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);

            user.setStatus(UserStatus.INACTIVE);
            assertThat(user.getStatus()).isEqualTo(UserStatus.INACTIVE);

            user.setStatus(UserStatus.LOCKED);
            assertThat(user.getStatus()).isEqualTo(UserStatus.LOCKED);
        }

        @Test
        @DisplayName("User is associated with a Tenant")
        void userShouldBeAssociatedWithTenant() {
            assertThat(user.getTenant()).isNotNull();
            assertThat(user.getTenant()).isEqualTo(tenant);
        }

        @Test
        @DisplayName("getFullName() should return first and last name combined")
        void getFullNameShouldReturnCombinedName() {
            user.setFirstName("John");
            user.setLastName("Doe");
            assertThat(user.getFullName()).isEqualTo("John Doe");
        }

        @Test
        @DisplayName("getFullName() should handle null last name")
        void getFullNameShouldHandleNullLastName() {
            user.setFirstName("John");
            user.setLastName(null);
            assertThat(user.getFullName()).isEqualTo("John");
        }

        @Nested
        @DisplayName("Login Attempt Tracking")
        class LoginAttemptTests {

            @Test
            @DisplayName("Failed login attempts should start at zero")
            void failedLoginAttemptsShouldStartAtZero() {
                assertThat(user.getFailedLoginAttempts()).isEqualTo(0);
            }

            @Test
            @DisplayName("incrementFailedLoginAttempts() should increment counter")
            void incrementShouldIncrementCounter() {
                user.incrementFailedLoginAttempts();
                assertThat(user.getFailedLoginAttempts()).isEqualTo(1);

                user.incrementFailedLoginAttempts();
                assertThat(user.getFailedLoginAttempts()).isEqualTo(2);
            }

            @Test
            @DisplayName("Account should lock after 5 failed attempts")
            void accountShouldLockAfterFiveFailedAttempts() {
                for (int i = 0; i < 5; i++) {
                    user.incrementFailedLoginAttempts();
                }

                assertThat(user.getFailedLoginAttempts()).isEqualTo(5);
                assertThat(user.getStatus()).isEqualTo(UserStatus.LOCKED);
            }

            @Test
            @DisplayName("Account should not lock before 5 failed attempts")
            void accountShouldNotLockBeforeFiveAttempts() {
                for (int i = 0; i < 4; i++) {
                    user.incrementFailedLoginAttempts();
                }

                assertThat(user.getFailedLoginAttempts()).isEqualTo(4);
                assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
            }

            @Test
            @DisplayName("resetFailedLoginAttempts() should reset counter and update lastLogin")
            void resetShouldClearCounterAndUpdateLogin() {
                user.incrementFailedLoginAttempts();
                user.incrementFailedLoginAttempts();
                assertThat(user.getFailedLoginAttempts()).isEqualTo(2);

                user.resetFailedLoginAttempts();

                assertThat(user.getFailedLoginAttempts()).isEqualTo(0);
                assertThat(user.getLastLogin()).isNotNull();
            }
        }
    }

    // ========================================================================
    // PAYMENT ENTITY
    // ========================================================================

    @Nested
    @DisplayName("Payment Entity")
    class PaymentEntityTests {

        private Order order;
        private Payment payment;

        @BeforeEach
        void setUp() {
            order = TestDataFactory.createOrder(tenant, customer, eventType);
            payment = TestDataFactory.createPayment(tenant, order);
        }

        @Test
        @DisplayName("Should create valid payment via factory")
        void shouldCreateValidPayment() {
            assertThat(payment).isNotNull();
            assertThat(payment.getId()).isNotNull();
            assertThat(payment.getPaymentNumber()).startsWith("PAY-");
            assertThat(payment.getOrder()).isEqualTo(order);
            assertThat(payment.getTenant()).isEqualTo(tenant);
            assertThat(payment.getPaymentDate()).isNotNull();
        }

        @Test
        @DisplayName("Payment amount should be set correctly")
        void paymentAmountShouldBeSetCorrectly() {
            assertThat(payment.getAmount()).isNotNull();
            assertThat(payment.getAmount()).isEqualByComparingTo(new BigDecimal("10000.00"));
        }

        @Test
        @DisplayName("Payment amount should be positive")
        void paymentAmountShouldBePositive() {
            assertThat(payment.getAmount().compareTo(BigDecimal.ZERO)).isGreaterThan(0);
        }

        @Test
        @DisplayName("Payment method should be set correctly from factory")
        void paymentMethodShouldBeSetCorrectly() {
            assertThat(payment.getPaymentMethod()).isEqualTo(PaymentMethod.CASH);
        }

        @Test
        @DisplayName("Payment should support all payment methods")
        void paymentShouldSupportAllPaymentMethods() {
            payment.setPaymentMethod(PaymentMethod.CASH);
            assertThat(payment.getPaymentMethod()).isEqualTo(PaymentMethod.CASH);

            payment.setPaymentMethod(PaymentMethod.UPI);
            assertThat(payment.getPaymentMethod()).isEqualTo(PaymentMethod.UPI);

            payment.setPaymentMethod(PaymentMethod.CARD);
            assertThat(payment.getPaymentMethod()).isEqualTo(PaymentMethod.CARD);

            payment.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
            assertThat(payment.getPaymentMethod()).isEqualTo(PaymentMethod.BANK_TRANSFER);

            payment.setPaymentMethod(PaymentMethod.CHEQUE);
            assertThat(payment.getPaymentMethod()).isEqualTo(PaymentMethod.CHEQUE);
        }

        @Test
        @DisplayName("Payment status should be COMPLETED from factory")
        void paymentStatusShouldBeCompletedFromFactory() {
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        }

        @Test
        @DisplayName("Payment should support all payment statuses")
        void paymentShouldSupportAllPaymentStatuses() {
            payment.setStatus(PaymentStatus.PENDING);
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);

            payment.setStatus(PaymentStatus.COMPLETED);
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);

            payment.setStatus(PaymentStatus.FAILED);
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
        }

        @Test
        @DisplayName("Payment should be associated with a Tenant")
        void paymentShouldBeAssociatedWithTenant() {
            assertThat(payment.getTenant()).isNotNull();
            assertThat(payment.getTenant()).isEqualTo(tenant);
        }

        @Test
        @DisplayName("Payment should be associated with an Order")
        void paymentShouldBeAssociatedWithOrder() {
            assertThat(payment.getOrder()).isNotNull();
            assertThat(payment.getOrder()).isEqualTo(order);
        }

        @Test
        @DisplayName("getOrderNumber() should return order's order number")
        void getOrderNumberShouldReturnOrdersOrderNumber() {
            assertThat(payment.getOrderNumber()).isEqualTo(order.getOrderNumber());
        }

        @Test
        @DisplayName("getOrderNumber() should return null when order is null")
        void getOrderNumberShouldReturnNullWhenOrderIsNull() {
            payment.setOrder(null);
            assertThat(payment.getOrderNumber()).isNull();
        }

        @Test
        @DisplayName("getCustomerName() should return customer name from order")
        void getCustomerNameShouldReturnCustomerName() {
            assertThat(payment.getCustomerName()).isEqualTo(customer.getName());
        }

        @Test
        @DisplayName("getCustomerName() should return null when order is null")
        void getCustomerNameShouldReturnNullWhenOrderIsNull() {
            payment.setOrder(null);
            assertThat(payment.getCustomerName()).isNull();
        }

        @Test
        @DisplayName("emailSent should default to false from factory")
        void emailSentShouldDefaultToFalse() {
            assertThat(payment.getEmailSent()).isFalse();
        }

        @Test
        @DisplayName("Payment builder should allow setting optional fields")
        void paymentBuilderShouldAllowOptionalFields() {
            Payment detailedPayment = Payment.builder()
                    .id(999L)
                    .tenant(tenant)
                    .order(order)
                    .paymentNumber("PAY-999999")
                    .amount(new BigDecimal("25000.00"))
                    .paymentMethod(PaymentMethod.UPI)
                    .paymentDate(LocalDate.now())
                    .status(PaymentStatus.COMPLETED)
                    .transactionReference("TXN-12345")
                    .upiId("merchant@upi")
                    .notes("Advance payment for wedding")
                    .build();

            assertThat(detailedPayment.getTransactionReference()).isEqualTo("TXN-12345");
            assertThat(detailedPayment.getUpiId()).isEqualTo("merchant@upi");
            assertThat(detailedPayment.getNotes()).isEqualTo("Advance payment for wedding");
            assertThat(detailedPayment.getPaymentMethod()).isEqualTo(PaymentMethod.UPI);
        }
    }

    // ========================================================================
    // BASE ENTITY COMMON TESTS
    // ========================================================================

    @Nested
    @DisplayName("BaseEntity Common Behavior")
    class BaseEntityTests {

        @Test
        @DisplayName("Entities should have version field for optimistic locking")
        void entitiesShouldHaveVersionField() {
            assertThat(tenant.getVersion()).isNotNull();
            assertThat(customer.getVersion()).isNotNull();
        }

        @Test
        @DisplayName("Entities should have audit timestamps")
        void entitiesShouldHaveAuditTimestamps() {
            assertThat(tenant.getCreatedAt()).isNotNull();
            assertThat(tenant.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("isDeleted() should return false for non-deleted entity")
        void isDeletedShouldReturnFalseForNonDeleted() {
            assertThat(tenant.isDeleted()).isFalse();
        }

        @Test
        @DisplayName("markAsDeleted() should set deletedAt timestamp")
        void markAsDeletedShouldSetTimestamp() {
            tenant.markAsDeleted();

            assertThat(tenant.isDeleted()).isTrue();
            assertThat(tenant.getDeletedAt()).isNotNull();
        }

        @Test
        @DisplayName("restore() should clear deletedAt timestamp")
        void restoreShouldClearTimestamp() {
            tenant.markAsDeleted();
            assertThat(tenant.isDeleted()).isTrue();

            tenant.restore();
            assertThat(tenant.isDeleted()).isFalse();
            assertThat(tenant.getDeletedAt()).isNull();
        }
    }
}
