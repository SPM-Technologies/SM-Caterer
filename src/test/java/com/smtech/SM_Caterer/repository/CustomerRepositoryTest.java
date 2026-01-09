package com.smtech.SM_Caterer.repository;

import com.smtech.SM_Caterer.base.BaseIntegrationTest;
import com.smtech.SM_Caterer.domain.entity.Customer;
import com.smtech.SM_Caterer.domain.entity.Tenant;
import com.smtech.SM_Caterer.domain.enums.Status;
import com.smtech.SM_Caterer.domain.enums.TenantStatus;
import com.smtech.SM_Caterer.domain.repository.CustomerRepository;
import com.smtech.SM_Caterer.domain.repository.TenantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CustomerRepository Integration Tests")
class CustomerRepositoryTest extends BaseIntegrationTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private TenantRepository tenantRepository;

    private Tenant tenant;

    @BeforeEach
    void setUp() {
        tenant = Tenant.builder()
                .tenantCode("TEST_TENANT")
                .businessName("Test Caterer")
                .email("test@caterer.com")
                .phone("9876543210")
                .status(TenantStatus.ACTIVE)
                .build();
        tenant = tenantRepository.save(tenant);
    }

    private Customer createCustomer(String code, String name) {
        return Customer.builder()
                .tenant(tenant)
                .customerCode(code)
                .name(name)
                .email(code.toLowerCase() + "@test.com")
                .phone("9876543210")
                .status(Status.ACTIVE)
                .build();
    }

    @Nested
    @DisplayName("Save Customer")
    class SaveCustomer {

        @Test
        @DisplayName("Should save customer successfully")
        void shouldSaveCustomerSuccessfully() {
            // Given
            Customer customer = createCustomer("CUST_001", "John Doe");

            // When
            Customer saved = customerRepository.save(customer);

            // Then
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getCustomerCode()).isEqualTo("CUST_001");
            assertThat(saved.getName()).isEqualTo("John Doe");
            assertThat(saved.getCreatedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Find Customer")
    class FindCustomer {

        @Test
        @DisplayName("Should find customer by tenant and code")
        void shouldFindByTenantAndCode() {
            // Given
            Customer customer = createCustomer("CUST_002", "Jane Doe");
            customerRepository.save(customer);

            // When
            Optional<Customer> found = customerRepository.findByTenantIdAndCustomerCode(tenant.getId(), "CUST_002");

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getName()).isEqualTo("Jane Doe");
        }

        @Test
        @DisplayName("Should find customers by tenant")
        void shouldFindByTenant() {
            // Given
            customerRepository.save(createCustomer("CUST_003", "Customer 1"));
            customerRepository.save(createCustomer("CUST_004", "Customer 2"));

            // When
            List<Customer> customers = customerRepository.findByTenantId(tenant.getId());

            // Then
            assertThat(customers).hasSize(2);
        }

        @Test
        @DisplayName("Should find customers by tenant with pagination")
        void shouldFindByTenantWithPagination() {
            // Given
            for (int i = 1; i <= 15; i++) {
                customerRepository.save(createCustomer("CUST_P" + i, "Customer " + i));
            }

            // When
            Page<Customer> page = customerRepository.findByTenantId(tenant.getId(), PageRequest.of(0, 10));

            // Then
            assertThat(page.getContent()).hasSize(10);
            assertThat(page.getTotalElements()).isEqualTo(15);
            assertThat(page.getTotalPages()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should find customers by status")
        void shouldFindByStatus() {
            // Given
            Customer active = createCustomer("CUST_ACT", "Active Customer");
            active.setStatus(Status.ACTIVE);
            customerRepository.save(active);

            Customer inactive = createCustomer("CUST_INA", "Inactive Customer");
            inactive.setStatus(Status.INACTIVE);
            customerRepository.save(inactive);

            // When
            List<Customer> activeCustomers = customerRepository.findByTenantIdAndStatus(tenant.getId(), Status.ACTIVE);

            // Then
            assertThat(activeCustomers).hasSize(1);
            assertThat(activeCustomers.get(0).getName()).isEqualTo("Active Customer");
        }

        @Test
        @DisplayName("Should search customers by name")
        void shouldSearchByName() {
            // Given
            customerRepository.save(createCustomer("CUST_S1", "John Smith"));
            customerRepository.save(createCustomer("CUST_S2", "Jane Smith"));
            customerRepository.save(createCustomer("CUST_S3", "Bob Jones"));

            // When
            List<Customer> smiths = customerRepository.searchByName(tenant.getId(), "Smith");

            // Then
            assertThat(smiths).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Check Existence")
    class CheckExistence {

        @Test
        @DisplayName("Should check if customer code exists")
        void shouldCheckIfCodeExists() {
            // Given
            customerRepository.save(createCustomer("CUST_EX", "Existing Customer"));

            // When/Then
            assertThat(customerRepository.existsByTenantIdAndCustomerCode(tenant.getId(), "CUST_EX")).isTrue();
            assertThat(customerRepository.existsByTenantIdAndCustomerCode(tenant.getId(), "NON_EXIST")).isFalse();
        }

        @Test
        @DisplayName("Should count customers by tenant")
        void shouldCountByTenant() {
            // Given
            customerRepository.save(createCustomer("CUST_C1", "Customer 1"));
            customerRepository.save(createCustomer("CUST_C2", "Customer 2"));

            // When
            long count = customerRepository.countByTenantId(tenant.getId());

            // Then
            assertThat(count).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Soft Delete")
    class SoftDelete {

        @Test
        @DisplayName("Should soft delete customer")
        void shouldSoftDeleteCustomer() {
            // Given
            Customer customer = createCustomer("CUST_DEL", "To Delete");
            customer = customerRepository.save(customer);

            // When
            customerRepository.delete(customer);

            // Then
            Optional<Customer> found = customerRepository.findById(customer.getId());
            assertThat(found).isEmpty();

            // Customer should still exist in DB with deleted_at set
            long count = customerRepository.countByTenantId(tenant.getId());
            assertThat(count).isEqualTo(0);
        }
    }
}
