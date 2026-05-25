package com.bruno.projeto_estagio_jacto.security

import com.bruno.projeto_estagio_jacto.entity.AccessLevel
import com.bruno.projeto_estagio_jacto.exception.ForbiddenOperationException
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class AuthorizationServiceTest {

    @Mock
    private lateinit var currentUserService: CurrentUserService

    @Test
    fun `manager should be allowed to perform write operations`() {
        val service = AuthorizationService(currentUserService)

        given(currentUserService.isManager()).willReturn(true)

        assertThatCode { service.ensureManager() }.doesNotThrowAnyException()
    }

    @Test
    fun `operator should not be allowed to perform write operations`() {
        val service = AuthorizationService(currentUserService)

        given(currentUserService.isManager()).willReturn(false)

        assertThatThrownBy { service.ensureManager() }
            .isInstanceOf(ForbiddenOperationException::class.java)
            .hasMessage("You do not have permission to perform this operation")
    }

    @Test
    fun `operator should not access another organization`() {
        val service = AuthorizationService(currentUserService)

        given(currentUserService.isManager()).willReturn(false)
        given(currentUserService.getCurrentOrganizationId()).willReturn(1L)

        assertThatThrownBy { service.ensureOrganizationAccess(2L) }
            .isInstanceOf(ForbiddenOperationException::class.java)
            .hasMessage("You do not have access to this organization")
    }

    @Test
    fun `manager should access any organization`() {
        val service = AuthorizationService(currentUserService)

        given(currentUserService.isManager()).willReturn(true)

        assertThatCode { service.ensureOrganizationAccess(99L) }.doesNotThrowAnyException()
    }
}
