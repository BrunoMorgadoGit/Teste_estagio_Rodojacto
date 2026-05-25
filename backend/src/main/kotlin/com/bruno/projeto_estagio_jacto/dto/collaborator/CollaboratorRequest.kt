package com.bruno.projeto_estagio_jacto.dto.collaborator

import com.bruno.projeto_estagio_jacto.entity.AccessLevel
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class CollaboratorRequest(
    @field:NotBlank(message = "Full name is required")
    val fullName: String,

    @field:Email(message = "Email must be valid")
    @field:NotBlank(message = "Email is required")
    val email: String,

    @field:NotBlank(message = "Password is required")
    @field:Size(min = 6, message = "Password must have at least 6 characters")
    val password: String,

    @field:NotNull(message = "Access level is required")
    val accessLevel: AccessLevel,

    @field:NotNull(message = "Organization id is required")
    val organizationId: Long,
)
