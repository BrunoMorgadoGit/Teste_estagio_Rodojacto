package com.bruno.projeto_estagio_jacto.mapper

import com.bruno.projeto_estagio_jacto.dto.organization.OrganizationRequest
import com.bruno.projeto_estagio_jacto.dto.organization.OrganizationResponse
import com.bruno.projeto_estagio_jacto.entity.Organization
import org.springframework.stereotype.Component

@Component
class OrganizationMapper {

    fun toEntity(request: OrganizationRequest): Organization =
        Organization(
            corporateName = request.corporateName,
            registrationCode = request.registrationCode,
        )

    fun updateEntity(entity: Organization, request: OrganizationRequest) {
        entity.corporateName = request.corporateName
        entity.registrationCode = request.registrationCode
    }

    fun toResponse(entity: Organization): OrganizationResponse =
        OrganizationResponse(
            id = entity.id ?: error("Organization id should not be null"),
            corporateName = entity.corporateName,
            registrationCode = entity.registrationCode,
            createdAt = entity.createdAt,
        )
}
