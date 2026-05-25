package com.bruno.projeto_estagio_jacto.dto.device

import java.time.LocalDateTime

data class DeviceResponse(
    val id: Long,
    val model: String,
    val assetTag: String,
    val organizationId: Long,
    val createdAt: LocalDateTime?,
)
