package com.bruno.projeto_estagio_jacto.service

import com.bruno.projeto_estagio_jacto.entity.Organization
import com.bruno.projeto_estagio_jacto.mapper.OrganizationMapper
import com.bruno.projeto_estagio_jacto.repository.OrganizationRepository
import com.bruno.projeto_estagio_jacto.security.AuthorizationService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class OrganizationServiceTest {

    @Mock
    private lateinit var organizationRepository: OrganizationRepository

    @Mock
    private lateinit var authorizationService: AuthorizationService

    @Test
    fun `manager should list all organizations`() {
        val organizationService = OrganizationService(organizationRepository, authorizationService, OrganizationMapper())
        val organizations = listOf(
            Organization(id = 1L, corporateName = "Jacto A", registrationCode = "A-001"),
            Organization(id = 2L, corporateName = "Jacto B", registrationCode = "B-001"),
        )

        given(authorizationService.isManager()).willReturn(true)
        given(organizationRepository.findAll()).willReturn(organizations)

        val response = organizationService.findAll()

        assertThat(response).hasSize(2)
        assertThat(response.map { it.id }).containsExactly(1L, 2L)
    }

    @Test
    fun `operator should list only its own organization`() {
        val organizationService = OrganizationService(organizationRepository, authorizationService, OrganizationMapper())
        val organization = Organization(id = 1L, corporateName = "Jacto A", registrationCode = "A-001")

        given(authorizationService.isManager()).willReturn(false)
        given(authorizationService.getCurrentOrganizationId()).willReturn(1L)
        given(organizationRepository.findById(1L)).willReturn(Optional.of(organization))

        val response = organizationService.findAll()

        assertThat(response).hasSize(1)
        assertThat(response.first().id).isEqualTo(1L)
        assertThat(response.first().corporateName).isEqualTo("Jacto A")
    }
}
