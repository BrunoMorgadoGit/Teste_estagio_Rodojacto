package com.bruno.projeto_estagio_jacto.mapper

import com.bruno.projeto_estagio_jacto.dto.device.DeviceRequest
import com.bruno.projeto_estagio_jacto.dto.device.DeviceResponse
import com.bruno.projeto_estagio_jacto.entity.Device
import com.bruno.projeto_estagio_jacto.entity.Organization
import org.springframework.stereotype.Component

@Component
class DeviceMapper {

    fun toEntity(request: DeviceRequest, organization: Organization): Device =
        Device(
            model = request.model,
            assetTag = request.assetTag,
            organization = organization,
        )

    fun updateEntity(entity: Device, request: DeviceRequest, organization: Organization) {
        entity.model = request.model
        entity.assetTag = request.assetTag
        entity.organization = organization
    }

    fun toResponse(entity: Device): DeviceResponse =
        DeviceResponse(
            id = entity.id ?: error("Device id should not be null"),
            model = entity.model,
            assetTag = entity.assetTag,
            organizationId = entity.organization.id ?: error("Organization id should not be null"),
            createdAt = entity.createdAt,
        )
}
