package com.bruno.projeto_estagio_jacto.dto.device

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class DeviceRequest(
    @field:NotBlank(message = "Model is required")
    val model: String,

    @field:NotBlank(message = "Asset tag is required")
    val assetTag: String,

    @field:NotNull(message = "Organization id is required")
    val organizationId: Long,
)
