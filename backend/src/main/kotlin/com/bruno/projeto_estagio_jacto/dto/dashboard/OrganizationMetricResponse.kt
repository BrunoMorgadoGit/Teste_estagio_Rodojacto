package com.bruno.projeto_estagio_jacto.dto.dashboard

data class OrganizationMetricResponse(
    val organizationId: Long,
    val organizationName: String,
    val total: Long,
)
