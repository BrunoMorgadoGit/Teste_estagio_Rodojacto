package com.bruno.projeto_estagio_jacto.controller

import com.bruno.projeto_estagio_jacto.dto.auth.AuthRequest
import com.bruno.projeto_estagio_jacto.dto.auth.AuthResponse
import com.bruno.projeto_estagio_jacto.dto.auth.AuthMeResponse
import com.bruno.projeto_estagio_jacto.service.AuthService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
) {

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    fun login(@Valid @RequestBody request: AuthRequest): AuthResponse =
        authService.login(request)

    @GetMapping("/me")
    fun me(): AuthMeResponse = authService.me()
}
