package com.bruno.projeto_estagio_jacto.service

import com.bruno.projeto_estagio_jacto.dto.auth.AuthRequest
import com.bruno.projeto_estagio_jacto.dto.auth.AuthMeResponse
import com.bruno.projeto_estagio_jacto.dto.auth.AuthResponse
import com.bruno.projeto_estagio_jacto.exception.BusinessException
import com.bruno.projeto_estagio_jacto.exception.InvalidCredentialsException
import com.bruno.projeto_estagio_jacto.mapper.CollaboratorMapper
import com.bruno.projeto_estagio_jacto.repository.CollaboratorRepository
import com.bruno.projeto_estagio_jacto.security.CurrentUserService
import com.bruno.projeto_estagio_jacto.security.JwtTokenProvider
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val collaboratorRepository: CollaboratorRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider,
    private val currentUserService: CurrentUserService,
    private val collaboratorMapper: CollaboratorMapper,
) {

    fun login(request: AuthRequest): AuthResponse {
        val collaborator = collaboratorRepository.findByEmail(request.email)
            ?: throw InvalidCredentialsException("Invalid credentials")

        if (!passwordEncoder.matches(request.password, collaborator.password)) {
            throw InvalidCredentialsException("Invalid credentials")
        }

        return AuthResponse(accessToken = jwtTokenProvider.generateToken(collaborator))
    }

    fun me(): AuthMeResponse {
        val email = currentUserService.getCurrentUser().username
        val collaborator = collaboratorRepository.findByEmail(email)
            ?: throw BusinessException("Authenticated user not found")

        return collaboratorMapper.toAuthMeResponse(collaborator)
    }
}
