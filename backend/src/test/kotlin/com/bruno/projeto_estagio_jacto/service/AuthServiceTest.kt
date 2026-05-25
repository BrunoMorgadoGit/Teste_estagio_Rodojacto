package com.bruno.projeto_estagio_jacto.service

import com.bruno.projeto_estagio_jacto.dto.auth.AuthRequest
import com.bruno.projeto_estagio_jacto.entity.AccessLevel
import com.bruno.projeto_estagio_jacto.entity.Collaborator
import com.bruno.projeto_estagio_jacto.entity.Organization
import com.bruno.projeto_estagio_jacto.exception.InvalidCredentialsException
import com.bruno.projeto_estagio_jacto.mapper.CollaboratorMapper
import com.bruno.projeto_estagio_jacto.repository.CollaboratorRepository
import com.bruno.projeto_estagio_jacto.security.CurrentUserService
import com.bruno.projeto_estagio_jacto.security.JwtTokenProvider
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.security.crypto.password.PasswordEncoder

@ExtendWith(MockitoExtension::class)
class AuthServiceTest {

    @Mock
    private lateinit var collaboratorRepository: CollaboratorRepository

    @Mock
    private lateinit var passwordEncoder: PasswordEncoder

    @Mock
    private lateinit var jwtTokenProvider: JwtTokenProvider

    @Mock
    private lateinit var currentUserService: CurrentUserService

    @Test
    fun `should return jwt token when credentials are valid`() {
        val organization = Organization(id = 1L, corporateName = "Jacto", registrationCode = "123")
        val collaborator = Collaborator(
            id = 10L,
            fullName = "Manager User",
            email = "manager@jacto.com",
            password = "encoded-password",
            accessLevel = AccessLevel.MANAGER,
            organization = organization,
        )
        val authService = AuthService(
            collaboratorRepository,
            passwordEncoder,
            jwtTokenProvider,
            currentUserService,
            CollaboratorMapper(),
        )

        given(collaboratorRepository.findByEmail("manager@jacto.com")).willReturn(collaborator)
        given(passwordEncoder.matches("plain-password", "encoded-password")).willReturn(true)
        given(jwtTokenProvider.generateToken(collaborator)).willReturn("jwt-token")

        val response = authService.login(AuthRequest(email = "manager@jacto.com", password = "plain-password"))

        assertThat(response.accessToken).isEqualTo("jwt-token")
        assertThat(response.tokenType).isEqualTo("Bearer")
    }

    @Test
    fun `should throw invalid credentials exception when collaborator is not found`() {
        val authService = AuthService(
            collaboratorRepository,
            passwordEncoder,
            jwtTokenProvider,
            currentUserService,
            CollaboratorMapper(),
        )

        given(collaboratorRepository.findByEmail("missing@jacto.com")).willReturn(null)

        assertThatThrownBy {
            authService.login(AuthRequest(email = "missing@jacto.com", password = "123456"))
        }
            .isInstanceOf(InvalidCredentialsException::class.java)
            .hasMessage("Invalid credentials")
    }

    @Test
    fun `should throw invalid credentials exception when password does not match`() {
        val organization = Organization(id = 1L, corporateName = "Jacto", registrationCode = "123")
        val collaborator = Collaborator(
            id = 10L,
            fullName = "Operator User",
            email = "operator@jacto.com",
            password = "encoded-password",
            accessLevel = AccessLevel.OPERATOR,
            organization = organization,
        )
        val authService = AuthService(
            collaboratorRepository,
            passwordEncoder,
            jwtTokenProvider,
            currentUserService,
            CollaboratorMapper(),
        )

        given(collaboratorRepository.findByEmail("operator@jacto.com")).willReturn(collaborator)
        given(passwordEncoder.matches("wrong-password", "encoded-password")).willReturn(false)

        assertThatThrownBy {
            authService.login(AuthRequest(email = "operator@jacto.com", password = "wrong-password"))
        }
            .isInstanceOf(InvalidCredentialsException::class.java)
            .hasMessage("Invalid credentials")
    }
}
