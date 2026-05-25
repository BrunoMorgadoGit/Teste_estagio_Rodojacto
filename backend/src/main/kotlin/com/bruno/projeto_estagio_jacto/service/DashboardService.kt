package com.bruno.projeto_estagio_jacto.service

import com.bruno.projeto_estagio_jacto.dto.dashboard.DashboardSummaryResponse
import com.bruno.projeto_estagio_jacto.repository.CollaboratorRepository
import com.bruno.projeto_estagio_jacto.repository.DeviceRepository
import com.bruno.projeto_estagio_jacto.repository.OrganizationRepository
import com.bruno.projeto_estagio_jacto.entity.AccessLevel
import com.bruno.projeto_estagio_jacto.security.AuthorizationService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DashboardService(
    private val organizationRepository: OrganizationRepository,
    private val collaboratorRepository: CollaboratorRepository,
    private val deviceRepository: DeviceRepository,
    private val organizationService: OrganizationService,
    private val authorizationService: AuthorizationService,
) {

    @Transactional(readOnly = true)
    fun getSummary(): DashboardSummaryResponse =
        if (authorizationService.isManager()) {
            DashboardSummaryResponse(
                totalOrganizations = organizationRepository.count(),
                totalCollaborators = collaboratorRepository.count(),
                totalDevices = deviceRepository.count(),
                totalManagers = collaboratorRepository.countByAccessLevel(AccessLevel.MANAGER),
                totalOperators = collaboratorRepository.countByAccessLevel(AccessLevel.OPERATOR),
                devicesByOrganization = deviceRepository.countDevicesByOrganization(),
                collaboratorsByOrganization = collaboratorRepository.countCollaboratorsByOrganization(),
            )
        } else {
            val organization = organizationService.findEntityById(authorizationService.getCurrentOrganizationId())
            val organizationId = organization.id ?: error("Organization id should not be null")

            DashboardSummaryResponse(
                organizationId = organizationId,
                organizationName = organization.corporateName,
                totalCollaborators = collaboratorRepository.countByOrganizationId(organizationId),
                totalDevices = deviceRepository.countByOrganizationId(organizationId),
                totalManagers = collaboratorRepository.countByOrganizationIdAndAccessLevel(organizationId, AccessLevel.MANAGER),
                totalOperators = collaboratorRepository.countByOrganizationIdAndAccessLevel(organizationId, AccessLevel.OPERATOR),
            )
        }
}
