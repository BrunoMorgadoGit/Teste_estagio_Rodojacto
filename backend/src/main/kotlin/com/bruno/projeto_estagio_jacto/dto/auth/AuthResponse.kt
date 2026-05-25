package com.bruno.projeto_estagio_jacto.dto.auth

data class AuthResponse(
    val accessToken: String,
    val tokenType: String = "Bearer",
)
