package com.bruno.projeto_estagio_jacto.dto.organization

import java.time.LocalDateTime

data class OrganizationResponse(
    val id: Long,
    val corporateName: String,
    val registrationCode: String,
    val createdAt: LocalDateTime?,
)
