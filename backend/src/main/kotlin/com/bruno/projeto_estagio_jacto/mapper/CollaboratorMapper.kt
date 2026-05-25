package com.bruno.projeto_estagio_jacto.mapper

import com.bruno.projeto_estagio_jacto.dto.auth.AuthMeResponse
import com.bruno.projeto_estagio_jacto.dto.collaborator.CollaboratorRequest
import com.bruno.projeto_estagio_jacto.dto.collaborator.CollaboratorResponse
import com.bruno.projeto_estagio_jacto.entity.Collaborator
import com.bruno.projeto_estagio_jacto.entity.Organization
import org.springframework.stereotype.Component

@Component
class CollaboratorMapper {

    fun toEntity(
        request: CollaboratorRequest,
        organization: Organization,
        encodedPassword: String,
    ): Collaborator =
        Collaborator(
            fullName = request.fullName,
            email = request.email,
            password = encodedPassword,
            accessLevel = request.accessLevel,
            organization = organization,
        )

    fun updateEntity(
        entity: Collaborator,
        request: CollaboratorRequest,
        organization: Organization,
        encodedPassword: String,
    ) {
        entity.fullName = request.fullName
        entity.email = request.email
        entity.password = encodedPassword
        entity.accessLevel = request.accessLevel
        entity.organization = organization
    }

    fun toResponse(entity: Collaborator): CollaboratorResponse =
        CollaboratorResponse(
            id = entity.id ?: error("Collaborator id should not be null"),
            fullName = entity.fullName,
            email = entity.email,
            accessLevel = entity.accessLevel,
            organizationId = entity.organization.id ?: error("Organization id should not be null"),
            createdAt = entity.createdAt,
        )

    fun toAuthMeResponse(entity: Collaborator): AuthMeResponse =
        AuthMeResponse(
            id = entity.id ?: error("Collaborator id should not be null"),
            fullName = entity.fullName,
            email = entity.email,
            accessLevel = entity.accessLevel,
            organizationId = entity.organization.id ?: error("Organization id should not be null"),
            organizationName = entity.organization.corporateName,
            createdAt = entity.createdAt,
        )
}
