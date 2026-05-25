package com.bruno.projeto_estagio_jacto.service

import com.bruno.projeto_estagio_jacto.dto.device.DeviceRequest
import com.bruno.projeto_estagio_jacto.dto.device.DeviceResponse
import com.bruno.projeto_estagio_jacto.entity.Device
import com.bruno.projeto_estagio_jacto.exception.DuplicateResourceException
import com.bruno.projeto_estagio_jacto.exception.ResourceNotFoundException
import com.bruno.projeto_estagio_jacto.mapper.DeviceMapper
import com.bruno.projeto_estagio_jacto.repository.DeviceRepository
import com.bruno.projeto_estagio_jacto.security.AuthorizationService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DeviceService(
    private val deviceRepository: DeviceRepository,
    private val organizationService: OrganizationService,
    private val authorizationService: AuthorizationService,
    private val deviceMapper: DeviceMapper,
) {

    @Transactional(readOnly = true)
    fun findAll(): List<DeviceResponse> =
        if (authorizationService.isManager()) {
            deviceRepository.findAll().map(deviceMapper::toResponse)
        } else {
            deviceRepository.findAllByOrganizationId(authorizationService.getCurrentOrganizationId())
                .map(deviceMapper::toResponse)
        }

    @Transactional(readOnly = true)
    fun findAllByOrganization(organizationId: Long): List<DeviceResponse> {
        authorizationService.ensureOrganizationAccess(organizationId)
        return deviceRepository.findAllByOrganizationId(organizationId).map(deviceMapper::toResponse)
    }

    @Transactional(readOnly = true)
    fun findById(id: Long): DeviceResponse {
        val device = findEntityById(id)
        authorizationService.ensureOrganizationAccess(device.organization.id ?: error("Organization id should not be null"))
        return deviceMapper.toResponse(device)
    }

    @Transactional
    fun create(request: DeviceRequest): DeviceResponse {
        authorizationService.ensureManager()

        if (deviceRepository.existsByAssetTag(request.assetTag)) {
            throw DuplicateResourceException("Asset tag already in use")
        }

        val organization = organizationService.findEntityById(request.organizationId)
        val device = deviceMapper.toEntity(request, organization)

        return deviceMapper.toResponse(deviceRepository.save(device))
    }

    @Transactional
    fun update(id: Long, request: DeviceRequest): DeviceResponse {
        authorizationService.ensureManager()
        val device = findEntityById(id)

        if (device.assetTag != request.assetTag && deviceRepository.existsByAssetTag(request.assetTag)) {
            throw DuplicateResourceException("Asset tag already in use")
        }

        deviceMapper.updateEntity(device, request, organizationService.findEntityById(request.organizationId))

        return deviceMapper.toResponse(deviceRepository.save(device))
    }

    @Transactional
    fun delete(id: Long) {
        authorizationService.ensureManager()
        val device = findEntityById(id)
        deviceRepository.delete(device)
    }

    @Transactional(readOnly = true)
    fun findEntityById(id: Long): Device =
        deviceRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Device with id $id not found") }
}
