package com.smtech.SM_Caterer.service;

import com.smtech.SM_Caterer.base.BaseUnitTest;
import com.smtech.SM_Caterer.domain.entity.Tenant;
import com.smtech.SM_Caterer.domain.repository.TenantRepository;
import com.smtech.SM_Caterer.exception.ResourceNotFoundException;
import com.smtech.SM_Caterer.fixtures.TestDataFactory;
import com.smtech.SM_Caterer.service.dto.TenantDTO;
import com.smtech.SM_Caterer.service.impl.TenantServiceImpl;
import com.smtech.SM_Caterer.service.mapper.TenantMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@DisplayName("TenantService Tests")
class TenantServiceTest extends BaseUnitTest {

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private TenantMapper tenantMapper;

    @InjectMocks
    private TenantServiceImpl tenantService;

    private Tenant tenant;
    private TenantDTO tenantDTO;

    @BeforeEach
    void setUp() {
        TestDataFactory.resetIdGenerators();
        tenant = TestDataFactory.createTenant();
        tenantDTO = TestDataFactory.createTenantDTO();
    }

    @Nested
    @DisplayName("Create Tenant")
    class CreateTenant {

        @Test
        @DisplayName("Should create tenant successfully")
        void shouldCreateTenantSuccessfully() {
            // Given
            when(tenantMapper.toEntity(any(TenantDTO.class))).thenReturn(tenant);
            when(tenantRepository.save(any(Tenant.class))).thenReturn(tenant);
            when(tenantMapper.toDto(any(Tenant.class))).thenReturn(tenantDTO);

            // When
            TenantDTO result = tenantService.create(tenantDTO);

            // Then
            assertThat(result).isNotNull();
            verify(tenantRepository).save(any(Tenant.class));
        }
    }

    @Nested
    @DisplayName("Find Tenant")
    class FindTenant {

        @Test
        @DisplayName("Should find tenant by ID")
        void shouldFindTenantById() {
            // Given
            when(tenantRepository.findById(anyLong())).thenReturn(Optional.of(tenant));
            when(tenantMapper.toDto(any(Tenant.class))).thenReturn(tenantDTO);

            // When
            Optional<TenantDTO> result = tenantService.findById(1L);

            // Then
            assertThat(result).isPresent();
            verify(tenantRepository).findById(1L);
        }

        @Test
        @DisplayName("Should return empty when tenant not found")
        void shouldReturnEmptyWhenNotFound() {
            // Given
            when(tenantRepository.findById(anyLong())).thenReturn(Optional.empty());

            // When
            Optional<TenantDTO> result = tenantService.findById(999L);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should find tenant by code")
        void shouldFindByTenantCode() {
            // Given
            when(tenantRepository.findByTenantCode(anyString())).thenReturn(Optional.of(tenant));
            when(tenantMapper.toDto(any(Tenant.class))).thenReturn(tenantDTO);

            // When
            Optional<TenantDTO> result = tenantService.findByTenantCode("TENANT_1");

            // Then
            assertThat(result).isPresent();
            verify(tenantRepository).findByTenantCode("TENANT_1");
        }
    }

    @Nested
    @DisplayName("Delete Tenant")
    class DeleteTenant {

        @Test
        @DisplayName("Should delete tenant successfully")
        void shouldDeleteTenantSuccessfully() {
            // Given
            when(tenantRepository.existsById(anyLong())).thenReturn(true);
            doNothing().when(tenantRepository).deleteById(anyLong());

            // When
            tenantService.delete(1L);

            // Then
            verify(tenantRepository).deleteById(1L);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when tenant not found")
        void shouldThrowExceptionWhenTenantNotFound() {
            // Given
            when(tenantRepository.existsById(anyLong())).thenReturn(false);

            // When/Then
            assertThatThrownBy(() -> tenantService.delete(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
