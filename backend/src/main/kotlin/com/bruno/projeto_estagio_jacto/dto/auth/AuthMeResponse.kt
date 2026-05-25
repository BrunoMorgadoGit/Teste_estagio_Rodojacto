package com.bruno.projeto_estagio_jacto.dto.auth

import com.bruno.projeto_estagio_jacto.entity.AccessLevel
import java.time.LocalDateTime

data class AuthMeResponse(
    val id: Long,
    val fullName: String,
    val email: String,
    val accessLevel: AccessLevel,
    val organizationId: Long,
    val organizationName: String,
    val createdAt: LocalDateTime?,
)
