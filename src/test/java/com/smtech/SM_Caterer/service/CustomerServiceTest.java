package com.smtech.SM_Caterer.service;

import com.smtech.SM_Caterer.base.BaseUnitTest;
import com.smtech.SM_Caterer.domain.entity.Customer;
import com.smtech.SM_Caterer.domain.entity.Tenant;
import com.smtech.SM_Caterer.domain.repository.CustomerRepository;
import com.smtech.SM_Caterer.domain.repository.TenantRepository;
import com.smtech.SM_Caterer.exception.DuplicateResourceException;
import com.smtech.SM_Caterer.exception.ResourceNotFoundException;
import com.smtech.SM_Caterer.fixtures.TestDataFactory;
import com.smtech.SM_Caterer.service.dto.CustomerDTO;
import com.smtech.SM_Caterer.service.impl.CustomerServiceImpl;
import com.smtech.SM_Caterer.service.mapper.CustomerMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("CustomerService Tests")
class CustomerServiceTest extends BaseUnitTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerMapper customerMapper;

    @Mock
    private TenantRepository tenantRepository;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private Tenant tenant;
    private Customer customer;
    private CustomerDTO customerDTO;

    @BeforeEach
    void setUp() {
        TestDataFactory.resetIdGenerators();
        tenant = TestDataFactory.createTenant();
        customer = TestDataFactory.createCustomer(tenant);
        customerDTO = TestDataFactory.createCustomerDTO(tenant.getId());
    }

    @Nested
    @DisplayName("Create Customer")
    class CreateCustomer {

        @Test
        @DisplayName("Should create customer successfully")
        void shouldCreateCustomerSuccessfully() {
            // Given
            when(customerRepository.existsByTenantIdAndCustomerCode(anyLong(), anyString())).thenReturn(false);
            when(tenantRepository.findById(anyLong())).thenReturn(Optional.of(tenant));
            when(customerMapper.toEntity(any(CustomerDTO.class))).thenReturn(customer);
            when(customerRepository.save(any(Customer.class))).thenReturn(customer);
            when(customerMapper.toDto(any(Customer.class))).thenReturn(customerDTO);

            // When
            CustomerDTO result = customerService.create(customerDTO);

            // Then
            assertThat(result).isNotNull();
            verify(customerRepository).existsByTenantIdAndCustomerCode(customerDTO.getTenantId(), customerDTO.getCustomerCode());
            verify(customerRepository).save(any(Customer.class));
        }

        @Test
        @DisplayName("Should throw DuplicateResourceException when customer code exists")
        void shouldThrowExceptionWhenCustomerCodeExists() {
            // Given
            when(customerRepository.existsByTenantIdAndCustomerCode(anyLong(), anyString())).thenReturn(true);

            // When/Then
            assertThatThrownBy(() -> customerService.create(customerDTO))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("customerCode");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when tenant not found")
        void shouldThrowExceptionWhenTenantNotFound() {
            // Given
            when(customerRepository.existsByTenantIdAndCustomerCode(anyLong(), anyString())).thenReturn(false);
            when(tenantRepository.findById(anyLong())).thenReturn(Optional.empty());
            when(customerMapper.toEntity(any(CustomerDTO.class))).thenReturn(customer);

            // When/Then
            assertThatThrownBy(() -> customerService.create(customerDTO))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Tenant");
        }
    }

    @Nested
    @DisplayName("Find Customer")
    class FindCustomer {

        @Test
        @DisplayName("Should find customer by ID")
        void shouldFindCustomerById() {
            // Given
            when(customerRepository.findById(anyLong())).thenReturn(Optional.of(customer));
            when(customerMapper.toDto(any(Customer.class))).thenReturn(customerDTO);

            // When
            Optional<CustomerDTO> result = customerService.findById(1L);

            // Then
            assertThat(result).isPresent();
            verify(customerRepository).findById(1L);
        }

        @Test
        @DisplayName("Should return empty when customer not found")
        void shouldReturnEmptyWhenNotFound() {
            // Given
            when(customerRepository.findById(anyLong())).thenReturn(Optional.empty());

            // When
            Optional<CustomerDTO> result = customerService.findById(999L);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should find customer by tenant and customer code")
        void shouldFindByTenantAndCustomerCode() {
            // Given
            when(customerRepository.findByTenantIdAndCustomerCode(anyLong(), anyString()))
                    .thenReturn(Optional.of(customer));
            when(customerMapper.toDto(any(Customer.class))).thenReturn(customerDTO);

            // When
            Optional<CustomerDTO> result = customerService.findByTenantIdAndCustomerCode(1L, "CUST_001");

            // Then
            assertThat(result).isPresent();
            verify(customerRepository).findByTenantIdAndCustomerCode(1L, "CUST_001");
        }

        @Test
        @DisplayName("Should find customers by tenant")
        void shouldFindByTenant() {
            // Given
            List<Customer> customers = List.of(customer);
            when(customerRepository.findByTenantId(anyLong())).thenReturn(customers);
            when(customerMapper.toDto(anyList())).thenReturn(List.of(customerDTO));

            // When
            List<CustomerDTO> result = customerService.findByTenantId(1L);

            // Then
            assertThat(result).hasSize(1);
            verify(customerRepository).findByTenantId(1L);
        }

        @Test
        @DisplayName("Should find customers by phone")
        void shouldFindByPhone() {
            // Given
            List<Customer> customers = List.of(customer);
            when(customerRepository.findByPhone(anyString())).thenReturn(customers);
            when(customerMapper.toDto(anyList())).thenReturn(List.of(customerDTO));

            // When
            List<CustomerDTO> result = customerService.findByPhone("9876543210");

            // Then
            assertThat(result).hasSize(1);
            verify(customerRepository).findByPhone("9876543210");
        }
    }

    @Nested
    @DisplayName("Delete Customer")
    class DeleteCustomer {

        @Test
        @DisplayName("Should delete customer successfully")
        void shouldDeleteCustomerSuccessfully() {
            // Given
            when(customerRepository.existsById(anyLong())).thenReturn(true);
            doNothing().when(customerRepository).deleteById(anyLong());

            // When
            customerService.delete(1L);

            // Then
            verify(customerRepository).deleteById(1L);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when customer not found")
        void shouldThrowExceptionWhenCustomerNotFound() {
            // Given
            when(customerRepository.existsById(anyLong())).thenReturn(false);

            // When/Then
            assertThatThrownBy(() -> customerService.delete(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
