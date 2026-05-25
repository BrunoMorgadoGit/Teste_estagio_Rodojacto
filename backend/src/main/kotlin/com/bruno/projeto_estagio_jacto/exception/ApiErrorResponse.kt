package com.bruno.projeto_estagio_jacto.exception

import java.time.LocalDateTime

data class ApiErrorResponse(
    val status: Int,
    val error: String,
    val message: String,
    val path: String,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val fields: Map<String, String>? = null,
)
