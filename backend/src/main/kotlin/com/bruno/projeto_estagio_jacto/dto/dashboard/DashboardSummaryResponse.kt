package com.bruno.projeto_estagio_jacto.dto.dashboard

data class DashboardSummaryResponse(
    val organizationId: Long? = null,
    val organizationName: String? = null,
    val totalOrganizations: Long? = null,
    val totalCollaborators: Long,
    val totalDevices: Long,
    val totalManagers: Long,
    val totalOperators: Long,
    val devicesByOrganization: List<OrganizationMetricResponse>? = null,
    val collaboratorsByOrganization: List<OrganizationMetricResponse>? = null,
)
