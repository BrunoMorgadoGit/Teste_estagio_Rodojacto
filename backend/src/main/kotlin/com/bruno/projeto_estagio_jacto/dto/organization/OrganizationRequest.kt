package com.bruno.projeto_estagio_jacto.dto.organization

import jakarta.validation.constraints.NotBlank

data class OrganizationRequest(
    @field:NotBlank(message = "Corporate name is required")
    val corporateName: String,

    @field:NotBlank(message = "Registration code is required")
    val registrationCode: String,
)
