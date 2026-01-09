package com.smtech.SM_Caterer.service;

import com.smtech.SM_Caterer.base.BaseUnitTest;
import com.smtech.SM_Caterer.domain.entity.*;
import com.smtech.SM_Caterer.domain.enums.OrderStatus;
import com.smtech.SM_Caterer.domain.repository.*;
import com.smtech.SM_Caterer.exception.ResourceNotFoundException;
import com.smtech.SM_Caterer.fixtures.TestDataFactory;
import com.smtech.SM_Caterer.service.dto.OrderDTO;
import com.smtech.SM_Caterer.service.impl.OrderServiceImpl;
import com.smtech.SM_Caterer.service.mapper.OrderMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@DisplayName("OrderService Tests")
class OrderServiceTest extends BaseUnitTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private EventTypeRepository eventTypeRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Tenant tenant;
    private Customer customer;
    private EventType eventType;
    private Order order;
    private OrderDTO orderDTO;

    @BeforeEach
    void setUp() {
        TestDataFactory.resetIdGenerators();
        tenant = TestDataFactory.createTenant();
        customer = TestDataFactory.createCustomer(tenant);
        eventType = TestDataFactory.createEventType(tenant);
        order = TestDataFactory.createOrder(tenant, customer, eventType);
        orderDTO = TestDataFactory.createOrderDTO(tenant.getId(), customer.getId(), eventType.getId());
    }

    @Nested
    @DisplayName("Find Order")
    class FindOrder {

        @Test
        @DisplayName("Should find order by ID")
        void shouldFindOrderById() {
            // Given
            when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));
            when(orderMapper.toDto(any(Order.class))).thenReturn(orderDTO);

            // When
            Optional<OrderDTO> result = orderService.findById(1L);

            // Then
            assertThat(result).isPresent();
            verify(orderRepository).findById(1L);
        }

        @Test
        @DisplayName("Should return empty when order not found")
        void shouldReturnEmptyWhenNotFound() {
            // Given
            when(orderRepository.findById(anyLong())).thenReturn(Optional.empty());

            // When
            Optional<OrderDTO> result = orderService.findById(999L);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should find orders by tenant with pagination")
        void shouldFindByTenantWithPagination() {
            // Given
            Page<Order> orderPage = new PageImpl<>(List.of(order));
            when(orderRepository.findByTenantId(anyLong(), any(Pageable.class))).thenReturn(orderPage);
            when(orderMapper.toDto(any(Order.class))).thenReturn(orderDTO);

            // When
            Page<OrderDTO> result = orderService.findByTenantId(1L, Pageable.unpaged());

            // Then
            assertThat(result.getContent()).hasSize(1);
            verify(orderRepository).findByTenantId(eq(1L), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("Order Workflow")
    class OrderWorkflow {

        @Test
        @DisplayName("Should get order status correctly")
        void shouldGetOrderStatus() {
            // Given
            order.setStatus(OrderStatus.DRAFT);

            // When/Then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.DRAFT);
        }

        @Test
        @DisplayName("Order should be editable in DRAFT status")
        void orderShouldBeEditableInDraft() {
            // Given
            order.setStatus(OrderStatus.DRAFT);

            // When/Then
            assertThat(order.isEditable()).isTrue();
        }

        @Test
        @DisplayName("Order should be cancellable before completion")
        void orderShouldBeCancellableBeforeCompletion() {
            // Given
            order.setStatus(OrderStatus.CONFIRMED);

            // When/Then
            assertThat(order.isCancellable()).isTrue();
        }
    }

    @Nested
    @DisplayName("Delete Order")
    class DeleteOrder {

        @Test
        @DisplayName("Should delete order successfully")
        void shouldDeleteOrderSuccessfully() {
            // Given
            when(orderRepository.existsById(anyLong())).thenReturn(true);
            doNothing().when(orderRepository).deleteById(anyLong());

            // When
            orderService.delete(1L);

            // Then
            verify(orderRepository).deleteById(1L);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when order not found")
        void shouldThrowExceptionWhenOrderNotFound() {
            // Given
            when(orderRepository.existsById(anyLong())).thenReturn(false);

            // When/Then
            assertThatThrownBy(() -> orderService.delete(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
