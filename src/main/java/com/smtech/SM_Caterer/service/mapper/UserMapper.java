package com.smtech.SM_Caterer.service.mapper;

import com.smtech.SM_Caterer.domain.entity.User;
import com.smtech.SM_Caterer.service.dto.UserDTO;
import org.mapstruct.*;

/**
 * Mapper for User entity.
 *
 * CRITICAL:
 * - Maps tenant.id to tenantId (avoid circular reference)
 * - Ignores password when mapping to DTO (security)
 * - Maps tenant reference separately
 */
@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.WARN,
    uses = {TenantMapper.class}
)
public interface UserMapper extends EntityMapper<UserDTO, User> {

    /**
     * Maps User entity to DTO.
     * - Extracts tenant.id to tenantId
     * - Extracts tenant.tenantCode to tenantCode
     * - Ignores full tenant object
     * - Password already excluded by @JsonProperty(WRITE_ONLY)
     */
    @Mapping(target = "tenantId", source = "tenant.id")
    @Mapping(target = "tenantCode", source = "tenant.tenantCode")
    @Override
    UserDTO toDto(User entity);

    /**
     * Maps UserDTO to User entity.
     * - Tenant will be set separately in service layer
     * - Password will be encoded in service layer
     */
    @Mapping(target = "tenant", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Override
    User toEntity(UserDTO dto);
}
