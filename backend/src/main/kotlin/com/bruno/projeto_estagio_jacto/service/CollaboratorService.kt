package com.bruno.projeto_estagio_jacto.service

import com.bruno.projeto_estagio_jacto.dto.collaborator.CollaboratorRequest
import com.bruno.projeto_estagio_jacto.dto.collaborator.CollaboratorResponse
import com.bruno.projeto_estagio_jacto.entity.Collaborator
import com.bruno.projeto_estagio_jacto.exception.DuplicateResourceException
import com.bruno.projeto_estagio_jacto.exception.ResourceNotFoundException
import com.bruno.projeto_estagio_jacto.mapper.CollaboratorMapper
import com.bruno.projeto_estagio_jacto.repository.CollaboratorRepository
import com.bruno.projeto_estagio_jacto.security.AuthorizationService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CollaboratorService(
    private val collaboratorRepository: CollaboratorRepository,
    private val organizationService: OrganizationService,
    private val passwordEncoder: PasswordEncoder,
    private val authorizationService: AuthorizationService,
    private val collaboratorMapper: CollaboratorMapper,
) {

    @Transactional(readOnly = true)
    fun findAll(): List<CollaboratorResponse> =
        if (authorizationService.isManager()) {
            collaboratorRepository.findAll().map(collaboratorMapper::toResponse)
        } else {
            collaboratorRepository.findAllByOrganizationId(authorizationService.getCurrentOrganizationId())
                .map(collaboratorMapper::toResponse)
        }

    @Transactional(readOnly = true)
    fun findAllByOrganization(organizationId: Long): List<CollaboratorResponse> {
        authorizationService.ensureOrganizationAccess(organizationId)
        return collaboratorRepository.findAllByOrganizationId(organizationId).map(collaboratorMapper::toResponse)
    }

    @Transactional(readOnly = true)
    fun findById(id: Long): CollaboratorResponse {
        val collaborator = findEntityById(id)
        authorizationService.ensureOrganizationAccess(collaborator.organization.id ?: error("Organization id should not be null"))
        return collaboratorMapper.toResponse(collaborator)
    }

    @Transactional
    fun create(request: CollaboratorRequest): CollaboratorResponse {
        authorizationService.ensureManager()

        if (collaboratorRepository.existsByEmail(request.email)) {
            throw DuplicateResourceException("Email already in use")
        }

        val organization = organizationService.findEntityById(request.organizationId)
        val collaborator = collaboratorMapper.toEntity(
            request = request,
            organization = organization,
            encodedPassword = passwordEncoder.encode(request.password),
        )

        return collaboratorMapper.toResponse(collaboratorRepository.save(collaborator))
    }

    @Transactional
    fun update(id: Long, request: CollaboratorRequest): CollaboratorResponse {
        authorizationService.ensureManager()
        val collaborator = findEntityById(id)

        if (collaborator.email != request.email && collaboratorRepository.existsByEmail(request.email)) {
            throw DuplicateResourceException("Email already in use")
        }

        collaboratorMapper.updateEntity(
            entity = collaborator,
            request = request,
            organization = organizationService.findEntityById(request.organizationId),
            encodedPassword = passwordEncoder.encode(request.password),
        )

        return collaboratorMapper.toResponse(collaboratorRepository.save(collaborator))
    }

    @Transactional
    fun delete(id: Long) {
        authorizationService.ensureManager()
        val collaborator = findEntityById(id)
        collaboratorRepository.delete(collaborator)
    }

    @Transactional(readOnly = true)
    fun findEntityById(id: Long): Collaborator =
        collaboratorRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Collaborator with id $id not found") }
}
