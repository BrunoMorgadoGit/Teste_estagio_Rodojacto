package com.bruno.projeto_estagio_jacto.dto.collaborator

import com.bruno.projeto_estagio_jacto.entity.AccessLevel
import java.time.LocalDateTime

data class CollaboratorResponse(
    val id: Long,
    val fullName: String,
    val email: String,
    val accessLevel: AccessLevel,
    val organizationId: Long,
    val createdAt: LocalDateTime?,
)
