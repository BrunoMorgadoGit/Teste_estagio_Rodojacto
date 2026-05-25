package com.bruno.projeto_estagio_jacto.service

import com.bruno.projeto_estagio_jacto.entity.AccessLevel
import com.bruno.projeto_estagio_jacto.entity.Collaborator
import com.bruno.projeto_estagio_jacto.entity.Organization
import com.bruno.projeto_estagio_jacto.exception.ForbiddenOperationException
import com.bruno.projeto_estagio_jacto.mapper.CollaboratorMapper
import com.bruno.projeto_estagio_jacto.repository.CollaboratorRepository
import com.bruno.projeto_estagio_jacto.security.AuthorizationService
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class CollaboratorServiceTest {

    @Mock
    private lateinit var collaboratorRepository: CollaboratorRepository

    @Mock
    private lateinit var organizationService: OrganizationService

    @Mock
    private lateinit var passwordEncoder: PasswordEncoder

    @Mock
    private lateinit var authorizationService: AuthorizationService

    @Test
    fun `operator should list only collaborators from its own organization`() {
        val service = CollaboratorService(
            collaboratorRepository,
            organizationService,
            passwordEncoder,
            authorizationService,
            CollaboratorMapper(),
        )
        val organization = Organization(id = 1L, corporateName = "Jacto A", registrationCode = "A-001")
        val collaborators = listOf(
            Collaborator(
                id = 1L,
                fullName = "Operator One",
                email = "one@jacto.com",
                password = "encoded",
                accessLevel = AccessLevel.OPERATOR,
                organization = organization,
            ),
        )

        given(authorizationService.isManager()).willReturn(false)
        given(authorizationService.getCurrentOrganizationId()).willReturn(1L)
        given(collaboratorRepository.findAllByOrganizationId(1L)).willReturn(collaborators)

        val response = service.findAll()

        assertThat(response).hasSize(1)
        assertThat(response.first().organizationId).isEqualTo(1L)
    }

    @Test
    fun `operator should not access collaborator from another organization`() {
        val service = CollaboratorService(
            collaboratorRepository,
            organizationService,
            passwordEncoder,
            authorizationService,
            CollaboratorMapper(),
        )
        val organization = Organization(id = 2L, corporateName = "Jacto B", registrationCode = "B-001")
        val collaborator = Collaborator(
            id = 2L,
            fullName = "Foreign User",
            email = "foreign@jacto.com",
            password = "encoded",
            accessLevel = AccessLevel.OPERATOR,
            organization = organization,
        )

        given(collaboratorRepository.findById(2L)).willReturn(Optional.of(collaborator))
        given(authorizationService.ensureOrganizationAccess(2L))
            .willThrow(ForbiddenOperationException("You do not have access to this organization"))

        assertThatThrownBy { service.findById(2L) }
            .isInstanceOf(ForbiddenOperationException::class.java)
            .hasMessage("You do not have access to this organization")
    }
}
