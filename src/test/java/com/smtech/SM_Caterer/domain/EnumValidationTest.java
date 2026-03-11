package com.smtech.SM_Caterer.domain;

import com.smtech.SM_Caterer.base.BaseUnitTest;
import com.smtech.SM_Caterer.domain.enums.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Comprehensive tests for all domain enums.
 * Validates enum constants, count, valueOf(), name(), and custom behaviors.
 */
@DisplayName("Enum Validation Tests")
class EnumValidationTest extends BaseUnitTest {

    // ========================================================================
    // UserRole
    // ========================================================================

    @Nested
    @DisplayName("UserRole Enum")
    class UserRoleTests {

        @Test
        @DisplayName("Should contain exactly 5 values")
        void shouldContainCorrectNumberOfValues() {
            assertThat(UserRole.values()).hasSize(5);
        }

        @Test
        @DisplayName("Should contain all expected constants")
        void shouldContainAllExpectedConstants() {
            assertThat(UserRole.values()).containsExactly(
                    UserRole.SUPER_ADMIN,
                    UserRole.TENANT_ADMIN,
                    UserRole.MANAGER,
                    UserRole.STAFF,
                    UserRole.VIEWER
            );
        }

        @ParameterizedTest
        @EnumSource(UserRole.class)
        @DisplayName("valueOf() should return correct enum for each constant name")
        void valueOfShouldReturnCorrectEnum(UserRole role) {
            assertThat(UserRole.valueOf(role.name())).isEqualTo(role);
        }

        @Test
        @DisplayName("valueOf() should resolve each known value")
        void valueOfShouldResolveKnownValues() {
            assertThat(UserRole.valueOf("SUPER_ADMIN")).isEqualTo(UserRole.SUPER_ADMIN);
            assertThat(UserRole.valueOf("TENANT_ADMIN")).isEqualTo(UserRole.TENANT_ADMIN);
            assertThat(UserRole.valueOf("MANAGER")).isEqualTo(UserRole.MANAGER);
            assertThat(UserRole.valueOf("STAFF")).isEqualTo(UserRole.STAFF);
            assertThat(UserRole.valueOf("VIEWER")).isEqualTo(UserRole.VIEWER);
        }

        @Test
        @DisplayName("valueOf() should throw IllegalArgumentException for invalid value")
        void valueOfShouldThrowForInvalidValue() {
            assertThatThrownBy(() -> UserRole.valueOf("INVALID_ROLE"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @ParameterizedTest
        @EnumSource(UserRole.class)
        @DisplayName("name() should return correct string for each constant")
        void nameShouldReturnCorrectString(UserRole role) {
            assertThat(role.name()).isEqualTo(role.toString());
        }

        @Test
        @DisplayName("name() should return expected strings")
        void nameShouldReturnExpectedStrings() {
            assertThat(UserRole.SUPER_ADMIN.name()).isEqualTo("SUPER_ADMIN");
            assertThat(UserRole.TENANT_ADMIN.name()).isEqualTo("TENANT_ADMIN");
            assertThat(UserRole.MANAGER.name()).isEqualTo("MANAGER");
            assertThat(UserRole.STAFF.name()).isEqualTo("STAFF");
            assertThat(UserRole.VIEWER.name()).isEqualTo("VIEWER");
        }
    }

    // ========================================================================
    // OrderStatus
    // ========================================================================

    @Nested
    @DisplayName("OrderStatus Enum")
    class OrderStatusTests {

        @Test
        @DisplayName("Should contain exactly 6 values")
        void shouldContainCorrectNumberOfValues() {
            assertThat(OrderStatus.values()).hasSize(6);
        }

        @Test
        @DisplayName("Should contain all expected constants")
        void shouldContainAllExpectedConstants() {
            assertThat(OrderStatus.values()).containsExactly(
                    OrderStatus.DRAFT,
                    OrderStatus.PENDING,
                    OrderStatus.CONFIRMED,
                    OrderStatus.IN_PROGRESS,
                    OrderStatus.COMPLETED,
                    OrderStatus.CANCELLED
            );
        }

        @ParameterizedTest
        @EnumSource(OrderStatus.class)
        @DisplayName("valueOf() should return correct enum for each constant name")
        void valueOfShouldReturnCorrectEnum(OrderStatus status) {
            assertThat(OrderStatus.valueOf(status.name())).isEqualTo(status);
        }

        @Test
        @DisplayName("valueOf() should resolve each known value")
        void valueOfShouldResolveKnownValues() {
            assertThat(OrderStatus.valueOf("DRAFT")).isEqualTo(OrderStatus.DRAFT);
            assertThat(OrderStatus.valueOf("PENDING")).isEqualTo(OrderStatus.PENDING);
            assertThat(OrderStatus.valueOf("CONFIRMED")).isEqualTo(OrderStatus.CONFIRMED);
            assertThat(OrderStatus.valueOf("IN_PROGRESS")).isEqualTo(OrderStatus.IN_PROGRESS);
            assertThat(OrderStatus.valueOf("COMPLETED")).isEqualTo(OrderStatus.COMPLETED);
            assertThat(OrderStatus.valueOf("CANCELLED")).isEqualTo(OrderStatus.CANCELLED);
        }

        @Test
        @DisplayName("valueOf() should throw IllegalArgumentException for invalid value")
        void valueOfShouldThrowForInvalidValue() {
            assertThatThrownBy(() -> OrderStatus.valueOf("INVALID_STATUS"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @ParameterizedTest
        @EnumSource(OrderStatus.class)
        @DisplayName("name() should return correct string for each constant")
        void nameShouldReturnCorrectString(OrderStatus status) {
            assertThat(status.name()).isEqualTo(status.toString());
        }

        @Test
        @DisplayName("name() should return expected strings")
        void nameShouldReturnExpectedStrings() {
            assertThat(OrderStatus.DRAFT.name()).isEqualTo("DRAFT");
            assertThat(OrderStatus.PENDING.name()).isEqualTo("PENDING");
            assertThat(OrderStatus.CONFIRMED.name()).isEqualTo("CONFIRMED");
            assertThat(OrderStatus.IN_PROGRESS.name()).isEqualTo("IN_PROGRESS");
            assertThat(OrderStatus.COMPLETED.name()).isEqualTo("COMPLETED");
            assertThat(OrderStatus.CANCELLED.name()).isEqualTo("CANCELLED");
        }

        // ----- Custom behavior tests -----

        @Test
        @DisplayName("getDisplayName() should return human-readable display names")
        void getDisplayNameShouldReturnHumanReadableNames() {
            assertThat(OrderStatus.DRAFT.getDisplayName()).isEqualTo("Draft");
            assertThat(OrderStatus.PENDING.getDisplayName()).isEqualTo("Pending Approval");
            assertThat(OrderStatus.CONFIRMED.getDisplayName()).isEqualTo("Confirmed");
            assertThat(OrderStatus.IN_PROGRESS.getDisplayName()).isEqualTo("In Progress");
            assertThat(OrderStatus.COMPLETED.getDisplayName()).isEqualTo("Completed");
            assertThat(OrderStatus.CANCELLED.getDisplayName()).isEqualTo("Cancelled");
        }

        @Test
        @DisplayName("getDescription() should return meaningful descriptions")
        void getDescriptionShouldReturnMeaningfulDescriptions() {
            assertThat(OrderStatus.DRAFT.getDescription()).isEqualTo("Order being created");
            assertThat(OrderStatus.PENDING.getDescription()).isEqualTo("Awaiting manager approval");
            assertThat(OrderStatus.CONFIRMED.getDescription()).isEqualTo("Order approved and scheduled");
            assertThat(OrderStatus.IN_PROGRESS.getDescription()).isEqualTo("Event is ongoing");
            assertThat(OrderStatus.COMPLETED.getDescription()).isEqualTo("Event completed successfully");
            assertThat(OrderStatus.CANCELLED.getDescription()).isEqualTo("Order cancelled");
        }

        @Test
        @DisplayName("isEditable() should return true only for DRAFT and PENDING")
        void isEditableShouldReturnTrueOnlyForDraftAndPending() {
            assertThat(OrderStatus.DRAFT.isEditable()).isTrue();
            assertThat(OrderStatus.PENDING.isEditable()).isTrue();
            assertThat(OrderStatus.CONFIRMED.isEditable()).isFalse();
            assertThat(OrderStatus.IN_PROGRESS.isEditable()).isFalse();
            assertThat(OrderStatus.COMPLETED.isEditable()).isFalse();
            assertThat(OrderStatus.CANCELLED.isEditable()).isFalse();
        }

        @Test
        @DisplayName("isCancellable() should return true for all except COMPLETED and CANCELLED")
        void isCancellableShouldReturnTrueForNonTerminalStatuses() {
            assertThat(OrderStatus.DRAFT.isCancellable()).isTrue();
            assertThat(OrderStatus.PENDING.isCancellable()).isTrue();
            assertThat(OrderStatus.CONFIRMED.isCancellable()).isTrue();
            assertThat(OrderStatus.IN_PROGRESS.isCancellable()).isTrue();
            assertThat(OrderStatus.COMPLETED.isCancellable()).isFalse();
            assertThat(OrderStatus.CANCELLED.isCancellable()).isFalse();
        }

        @Test
        @DisplayName("isTerminal() should return true only for COMPLETED and CANCELLED")
        void isTerminalShouldReturnTrueOnlyForTerminalStatuses() {
            assertThat(OrderStatus.DRAFT.isTerminal()).isFalse();
            assertThat(OrderStatus.PENDING.isTerminal()).isFalse();
            assertThat(OrderStatus.CONFIRMED.isTerminal()).isFalse();
            assertThat(OrderStatus.IN_PROGRESS.isTerminal()).isFalse();
            assertThat(OrderStatus.COMPLETED.isTerminal()).isTrue();
            assertThat(OrderStatus.CANCELLED.isTerminal()).isTrue();
        }

        @Test
        @DisplayName("isActive() should be the inverse of isTerminal()")
        void isActiveShouldBeInverseOfTerminal() {
            for (OrderStatus status : OrderStatus.values()) {
                assertThat(status.isActive()).isEqualTo(!status.isTerminal());
            }
        }

        @Test
        @DisplayName("DRAFT should transition to PENDING or CANCELLED")
        void draftShouldTransitionToPendingOrCancelled() {
            assertThat(OrderStatus.DRAFT.getNextStatuses())
                    .containsExactly(OrderStatus.PENDING, OrderStatus.CANCELLED);
        }

        @Test
        @DisplayName("PENDING should transition to CONFIRMED or CANCELLED")
        void pendingShouldTransitionToConfirmedOrCancelled() {
            assertThat(OrderStatus.PENDING.getNextStatuses())
                    .containsExactly(OrderStatus.CONFIRMED, OrderStatus.CANCELLED);
        }

        @Test
        @DisplayName("CONFIRMED should transition to IN_PROGRESS or CANCELLED")
        void confirmedShouldTransitionToInProgressOrCancelled() {
            assertThat(OrderStatus.CONFIRMED.getNextStatuses())
                    .containsExactly(OrderStatus.IN_PROGRESS, OrderStatus.CANCELLED);
        }

        @Test
        @DisplayName("IN_PROGRESS should transition to COMPLETED or CANCELLED")
        void inProgressShouldTransitionToCompletedOrCancelled() {
            assertThat(OrderStatus.IN_PROGRESS.getNextStatuses())
                    .containsExactly(OrderStatus.COMPLETED, OrderStatus.CANCELLED);
        }

        @Test
        @DisplayName("Terminal statuses should have no next statuses")
        void terminalStatusesShouldHaveNoNextStatuses() {
            assertThat(OrderStatus.COMPLETED.getNextStatuses()).isEmpty();
            assertThat(OrderStatus.CANCELLED.getNextStatuses()).isEmpty();
        }

        @Test
        @DisplayName("canTransitionTo() should validate allowed transitions")
        void canTransitionToShouldValidateAllowedTransitions() {
            // Valid transitions
            assertThat(OrderStatus.DRAFT.canTransitionTo(OrderStatus.PENDING)).isTrue();
            assertThat(OrderStatus.PENDING.canTransitionTo(OrderStatus.CONFIRMED)).isTrue();
            assertThat(OrderStatus.CONFIRMED.canTransitionTo(OrderStatus.IN_PROGRESS)).isTrue();
            assertThat(OrderStatus.IN_PROGRESS.canTransitionTo(OrderStatus.COMPLETED)).isTrue();

            // Invalid transitions
            assertThat(OrderStatus.DRAFT.canTransitionTo(OrderStatus.COMPLETED)).isFalse();
            assertThat(OrderStatus.PENDING.canTransitionTo(OrderStatus.IN_PROGRESS)).isFalse();
            assertThat(OrderStatus.COMPLETED.canTransitionTo(OrderStatus.DRAFT)).isFalse();
            assertThat(OrderStatus.CANCELLED.canTransitionTo(OrderStatus.DRAFT)).isFalse();
        }

        @Test
        @DisplayName("canTransitionTo() should return false for null target")
        void canTransitionToShouldReturnFalseForNull() {
            for (OrderStatus status : OrderStatus.values()) {
                assertThat(status.canTransitionTo(null)).isFalse();
            }
        }

        @Test
        @DisplayName("All non-terminal statuses can transition to CANCELLED")
        void allNonTerminalStatusesShouldTransitionToCancelled() {
            assertThat(OrderStatus.DRAFT.canTransitionTo(OrderStatus.CANCELLED)).isTrue();
            assertThat(OrderStatus.PENDING.canTransitionTo(OrderStatus.CANCELLED)).isTrue();
            assertThat(OrderStatus.CONFIRMED.canTransitionTo(OrderStatus.CANCELLED)).isTrue();
            assertThat(OrderStatus.IN_PROGRESS.canTransitionTo(OrderStatus.CANCELLED)).isTrue();
        }

        @Test
        @DisplayName("getBadgeClass() should return appropriate Bootstrap classes")
        void getBadgeClassShouldReturnBootstrapClasses() {
            assertThat(OrderStatus.DRAFT.getBadgeClass()).isEqualTo("bg-secondary");
            assertThat(OrderStatus.PENDING.getBadgeClass()).isEqualTo("bg-warning text-dark");
            assertThat(OrderStatus.CONFIRMED.getBadgeClass()).isEqualTo("bg-info");
            assertThat(OrderStatus.IN_PROGRESS.getBadgeClass()).isEqualTo("bg-primary");
            assertThat(OrderStatus.COMPLETED.getBadgeClass()).isEqualTo("bg-success");
            assertThat(OrderStatus.CANCELLED.getBadgeClass()).isEqualTo("bg-danger");
        }
    }

    // ========================================================================
    // PaymentMethod
    // ========================================================================

    @Nested
    @DisplayName("PaymentMethod Enum")
    class PaymentMethodTests {

        @Test
        @DisplayName("Should contain exactly 5 values")
        void shouldContainCorrectNumberOfValues() {
            assertThat(PaymentMethod.values()).hasSize(5);
        }

        @Test
        @DisplayName("Should contain all expected constants")
        void shouldContainAllExpectedConstants() {
            assertThat(PaymentMethod.values()).containsExactly(
                    PaymentMethod.CASH,
                    PaymentMethod.UPI,
                    PaymentMethod.BANK_TRANSFER,
                    PaymentMethod.CARD,
                    PaymentMethod.CHEQUE
            );
        }

        @ParameterizedTest
        @EnumSource(PaymentMethod.class)
        @DisplayName("valueOf() should return correct enum for each constant name")
        void valueOfShouldReturnCorrectEnum(PaymentMethod method) {
            assertThat(PaymentMethod.valueOf(method.name())).isEqualTo(method);
        }

        @Test
        @DisplayName("valueOf() should resolve each known value")
        void valueOfShouldResolveKnownValues() {
            assertThat(PaymentMethod.valueOf("CASH")).isEqualTo(PaymentMethod.CASH);
            assertThat(PaymentMethod.valueOf("UPI")).isEqualTo(PaymentMethod.UPI);
            assertThat(PaymentMethod.valueOf("BANK_TRANSFER")).isEqualTo(PaymentMethod.BANK_TRANSFER);
            assertThat(PaymentMethod.valueOf("CARD")).isEqualTo(PaymentMethod.CARD);
            assertThat(PaymentMethod.valueOf("CHEQUE")).isEqualTo(PaymentMethod.CHEQUE);
        }

        @Test
        @DisplayName("valueOf() should throw IllegalArgumentException for invalid value")
        void valueOfShouldThrowForInvalidValue() {
            assertThatThrownBy(() -> PaymentMethod.valueOf("CRYPTO"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @ParameterizedTest
        @EnumSource(PaymentMethod.class)
        @DisplayName("name() should return correct string for each constant")
        void nameShouldReturnCorrectString(PaymentMethod method) {
            assertThat(method.name()).isEqualTo(method.toString());
        }

        @Test
        @DisplayName("name() should return expected strings")
        void nameShouldReturnExpectedStrings() {
            assertThat(PaymentMethod.CASH.name()).isEqualTo("CASH");
            assertThat(PaymentMethod.UPI.name()).isEqualTo("UPI");
            assertThat(PaymentMethod.BANK_TRANSFER.name()).isEqualTo("BANK_TRANSFER");
            assertThat(PaymentMethod.CARD.name()).isEqualTo("CARD");
            assertThat(PaymentMethod.CHEQUE.name()).isEqualTo("CHEQUE");
        }
    }

    // ========================================================================
    // PaymentStatus
    // ========================================================================

    @Nested
    @DisplayName("PaymentStatus Enum")
    class PaymentStatusTests {

        @Test
        @DisplayName("Should contain exactly 3 values")
        void shouldContainCorrectNumberOfValues() {
            assertThat(PaymentStatus.values()).hasSize(3);
        }

        @Test
        @DisplayName("Should contain all expected constants")
        void shouldContainAllExpectedConstants() {
            assertThat(PaymentStatus.values()).containsExactly(
                    PaymentStatus.PENDING,
                    PaymentStatus.COMPLETED,
                    PaymentStatus.FAILED
            );
        }

        @ParameterizedTest
        @EnumSource(PaymentStatus.class)
        @DisplayName("valueOf() should return correct enum for each constant name")
        void valueOfShouldReturnCorrectEnum(PaymentStatus status) {
            assertThat(PaymentStatus.valueOf(status.name())).isEqualTo(status);
        }

        @Test
        @DisplayName("valueOf() should resolve each known value")
        void valueOfShouldResolveKnownValues() {
            assertThat(PaymentStatus.valueOf("PENDING")).isEqualTo(PaymentStatus.PENDING);
            assertThat(PaymentStatus.valueOf("COMPLETED")).isEqualTo(PaymentStatus.COMPLETED);
            assertThat(PaymentStatus.valueOf("FAILED")).isEqualTo(PaymentStatus.FAILED);
        }

        @Test
        @DisplayName("valueOf() should throw IllegalArgumentException for invalid value")
        void valueOfShouldThrowForInvalidValue() {
            assertThatThrownBy(() -> PaymentStatus.valueOf("REFUNDED"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @ParameterizedTest
        @EnumSource(PaymentStatus.class)
        @DisplayName("name() should return correct string for each constant")
        void nameShouldReturnCorrectString(PaymentStatus status) {
            assertThat(status.name()).isEqualTo(status.toString());
        }

        @Test
        @DisplayName("name() should return expected strings")
        void nameShouldReturnExpectedStrings() {
            assertThat(PaymentStatus.PENDING.name()).isEqualTo("PENDING");
            assertThat(PaymentStatus.COMPLETED.name()).isEqualTo("COMPLETED");
            assertThat(PaymentStatus.FAILED.name()).isEqualTo("FAILED");
        }
    }

    // ========================================================================
    // Status
    // ========================================================================

    @Nested
    @DisplayName("Status Enum")
    class StatusTests {

        @Test
        @DisplayName("Should contain exactly 2 values")
        void shouldContainCorrectNumberOfValues() {
            assertThat(Status.values()).hasSize(2);
        }

        @Test
        @DisplayName("Should contain all expected constants")
        void shouldContainAllExpectedConstants() {
            assertThat(Status.values()).containsExactly(
                    Status.ACTIVE,
                    Status.INACTIVE
            );
        }

        @ParameterizedTest
        @EnumSource(Status.class)
        @DisplayName("valueOf() should return correct enum for each constant name")
        void valueOfShouldReturnCorrectEnum(Status status) {
            assertThat(Status.valueOf(status.name())).isEqualTo(status);
        }

        @Test
        @DisplayName("valueOf() should resolve each known value")
        void valueOfShouldResolveKnownValues() {
            assertThat(Status.valueOf("ACTIVE")).isEqualTo(Status.ACTIVE);
            assertThat(Status.valueOf("INACTIVE")).isEqualTo(Status.INACTIVE);
        }

        @Test
        @DisplayName("valueOf() should throw IllegalArgumentException for invalid value")
        void valueOfShouldThrowForInvalidValue() {
            assertThatThrownBy(() -> Status.valueOf("DELETED"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @ParameterizedTest
        @EnumSource(Status.class)
        @DisplayName("name() should return correct string for each constant")
        void nameShouldReturnCorrectString(Status status) {
            assertThat(status.name()).isEqualTo(status.toString());
        }

        @Test
        @DisplayName("name() should return expected strings")
        void nameShouldReturnExpectedStrings() {
            assertThat(Status.ACTIVE.name()).isEqualTo("ACTIVE");
            assertThat(Status.INACTIVE.name()).isEqualTo("INACTIVE");
        }
    }

    // ========================================================================
    // TenantStatus
    // ========================================================================

    @Nested
    @DisplayName("TenantStatus Enum")
    class TenantStatusTests {

        @Test
        @DisplayName("Should contain exactly 3 values")
        void shouldContainCorrectNumberOfValues() {
            assertThat(TenantStatus.values()).hasSize(3);
        }

        @Test
        @DisplayName("Should contain all expected constants")
        void shouldContainAllExpectedConstants() {
            assertThat(TenantStatus.values()).containsExactly(
                    TenantStatus.ACTIVE,
                    TenantStatus.INACTIVE,
                    TenantStatus.SUSPENDED
            );
        }

        @ParameterizedTest
        @EnumSource(TenantStatus.class)
        @DisplayName("valueOf() should return correct enum for each constant name")
        void valueOfShouldReturnCorrectEnum(TenantStatus status) {
            assertThat(TenantStatus.valueOf(status.name())).isEqualTo(status);
        }

        @Test
        @DisplayName("valueOf() should resolve each known value")
        void valueOfShouldResolveKnownValues() {
            assertThat(TenantStatus.valueOf("ACTIVE")).isEqualTo(TenantStatus.ACTIVE);
            assertThat(TenantStatus.valueOf("INACTIVE")).isEqualTo(TenantStatus.INACTIVE);
            assertThat(TenantStatus.valueOf("SUSPENDED")).isEqualTo(TenantStatus.SUSPENDED);
        }

        @Test
        @DisplayName("valueOf() should throw IllegalArgumentException for invalid value")
        void valueOfShouldThrowForInvalidValue() {
            assertThatThrownBy(() -> TenantStatus.valueOf("TRIAL"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @ParameterizedTest
        @EnumSource(TenantStatus.class)
        @DisplayName("name() should return correct string for each constant")
        void nameShouldReturnCorrectString(TenantStatus status) {
            assertThat(status.name()).isEqualTo(status.toString());
        }

        @Test
        @DisplayName("name() should return expected strings")
        void nameShouldReturnExpectedStrings() {
            assertThat(TenantStatus.ACTIVE.name()).isEqualTo("ACTIVE");
            assertThat(TenantStatus.INACTIVE.name()).isEqualTo("INACTIVE");
            assertThat(TenantStatus.SUSPENDED.name()).isEqualTo("SUSPENDED");
        }
    }

    // ========================================================================
    // UserStatus
    // ========================================================================

    @Nested
    @DisplayName("UserStatus Enum")
    class UserStatusTests {

        @Test
        @DisplayName("Should contain exactly 3 values")
        void shouldContainCorrectNumberOfValues() {
            assertThat(UserStatus.values()).hasSize(3);
        }

        @Test
        @DisplayName("Should contain all expected constants")
        void shouldContainAllExpectedConstants() {
            assertThat(UserStatus.values()).containsExactly(
                    UserStatus.ACTIVE,
                    UserStatus.INACTIVE,
                    UserStatus.LOCKED
            );
        }

        @ParameterizedTest
        @EnumSource(UserStatus.class)
        @DisplayName("valueOf() should return correct enum for each constant name")
        void valueOfShouldReturnCorrectEnum(UserStatus status) {
            assertThat(UserStatus.valueOf(status.name())).isEqualTo(status);
        }

        @Test
        @DisplayName("valueOf() should resolve each known value")
        void valueOfShouldResolveKnownValues() {
            assertThat(UserStatus.valueOf("ACTIVE")).isEqualTo(UserStatus.ACTIVE);
            assertThat(UserStatus.valueOf("INACTIVE")).isEqualTo(UserStatus.INACTIVE);
            assertThat(UserStatus.valueOf("LOCKED")).isEqualTo(UserStatus.LOCKED);
        }

        @Test
        @DisplayName("valueOf() should throw IllegalArgumentException for invalid value")
        void valueOfShouldThrowForInvalidValue() {
            assertThatThrownBy(() -> UserStatus.valueOf("BANNED"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @ParameterizedTest
        @EnumSource(UserStatus.class)
        @DisplayName("name() should return correct string for each constant")
        void nameShouldReturnCorrectString(UserStatus status) {
            assertThat(status.name()).isEqualTo(status.toString());
        }

        @Test
        @DisplayName("name() should return expected strings")
        void nameShouldReturnExpectedStrings() {
            assertThat(UserStatus.ACTIVE.name()).isEqualTo("ACTIVE");
            assertThat(UserStatus.INACTIVE.name()).isEqualTo("INACTIVE");
            assertThat(UserStatus.LOCKED.name()).isEqualTo("LOCKED");
        }
    }

    // ========================================================================
    // MenuCategory
    // ========================================================================

    @Nested
    @DisplayName("MenuCategory Enum")
    class MenuCategoryTests {

        @Test
        @DisplayName("Should contain exactly 3 values")
        void shouldContainCorrectNumberOfValues() {
            assertThat(MenuCategory.values()).hasSize(3);
        }

        @Test
        @DisplayName("Should contain all expected constants")
        void shouldContainAllExpectedConstants() {
            assertThat(MenuCategory.values()).containsExactly(
                    MenuCategory.VEG,
                    MenuCategory.NON_VEG,
                    MenuCategory.BOTH
            );
        }

        @ParameterizedTest
        @EnumSource(MenuCategory.class)
        @DisplayName("valueOf() should return correct enum for each constant name")
        void valueOfShouldReturnCorrectEnum(MenuCategory category) {
            assertThat(MenuCategory.valueOf(category.name())).isEqualTo(category);
        }

        @Test
        @DisplayName("valueOf() should resolve each known value")
        void valueOfShouldResolveKnownValues() {
            assertThat(MenuCategory.valueOf("VEG")).isEqualTo(MenuCategory.VEG);
            assertThat(MenuCategory.valueOf("NON_VEG")).isEqualTo(MenuCategory.NON_VEG);
            assertThat(MenuCategory.valueOf("BOTH")).isEqualTo(MenuCategory.BOTH);
        }

        @Test
        @DisplayName("valueOf() should throw IllegalArgumentException for invalid value")
        void valueOfShouldThrowForInvalidValue() {
            assertThatThrownBy(() -> MenuCategory.valueOf("VEGAN"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @ParameterizedTest
        @EnumSource(MenuCategory.class)
        @DisplayName("name() should return correct string for each constant")
        void nameShouldReturnCorrectString(MenuCategory category) {
            assertThat(category.name()).isEqualTo(category.toString());
        }

        @Test
        @DisplayName("name() should return expected strings")
        void nameShouldReturnExpectedStrings() {
            assertThat(MenuCategory.VEG.name()).isEqualTo("VEG");
            assertThat(MenuCategory.NON_VEG.name()).isEqualTo("NON_VEG");
            assertThat(MenuCategory.BOTH.name()).isEqualTo("BOTH");
        }
    }
}
