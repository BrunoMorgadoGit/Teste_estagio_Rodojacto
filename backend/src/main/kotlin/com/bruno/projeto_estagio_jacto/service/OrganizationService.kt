package com.bruno.projeto_estagio_jacto.service

import com.bruno.projeto_estagio_jacto.dto.organization.OrganizationRequest
import com.bruno.projeto_estagio_jacto.dto.organization.OrganizationResponse
import com.bruno.projeto_estagio_jacto.entity.Organization
import com.bruno.projeto_estagio_jacto.exception.DuplicateResourceException
import com.bruno.projeto_estagio_jacto.exception.ResourceNotFoundException
import com.bruno.projeto_estagio_jacto.mapper.OrganizationMapper
import com.bruno.projeto_estagio_jacto.repository.OrganizationRepository
import com.bruno.projeto_estagio_jacto.security.AuthorizationService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OrganizationService(
    private val organizationRepository: OrganizationRepository,
    private val authorizationService: AuthorizationService,
    private val organizationMapper: OrganizationMapper,
) {

    @Transactional(readOnly = true)
    fun findAll(): List<OrganizationResponse> {
        if (authorizationService.isManager()) {
            return organizationRepository.findAll().map(organizationMapper::toResponse)
        }

        return listOf(findById(authorizationService.getCurrentOrganizationId()))
    }

    @Transactional(readOnly = true)
    fun findById(id: Long): OrganizationResponse {
        val organization = findEntityById(id)
        authorizationService.ensureOrganizationAccess(id)
        return organizationMapper.toResponse(organization)
    }

    @Transactional
    fun create(request: OrganizationRequest): OrganizationResponse {
        authorizationService.ensureManager()

        if (organizationRepository.existsByRegistrationCode(request.registrationCode)) {
            throw DuplicateResourceException("Registration code already in use")
        }

        return organizationMapper.toResponse(organizationRepository.save(organizationMapper.toEntity(request)))
    }

    @Transactional
    fun update(id: Long, request: OrganizationRequest): OrganizationResponse {
        authorizationService.ensureManager()
        val organization = findEntityById(id)

        if (organization.registrationCode != request.registrationCode &&
            organizationRepository.existsByRegistrationCode(request.registrationCode)
        ) {
            throw DuplicateResourceException("Registration code already in use")
        }

        organizationMapper.updateEntity(organization, request)

        return organizationMapper.toResponse(organizationRepository.save(organization))
    }

    @Transactional
    fun delete(id: Long) {
        authorizationService.ensureManager()
        val organization = findEntityById(id)
        organizationRepository.delete(organization)
        organizationRepository.flush()
    }

    @Transactional(readOnly = true)
    fun findEntityById(id: Long): Organization =
        organizationRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Organization with id $id not found") }
}
