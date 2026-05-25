package com.bruno.projeto_estagio_jacto.security

import com.bruno.projeto_estagio_jacto.exception.ForbiddenOperationException
import org.springframework.stereotype.Service

@Service
class AuthorizationService(
    private val currentUserService: CurrentUserService,
) {

    fun isManager(): Boolean = currentUserService.isManager()

    fun getCurrentOrganizationId(): Long = currentUserService.getCurrentOrganizationId()

    fun ensureManager() {
        if (!isManager()) {
            throw ForbiddenOperationException("You do not have permission to perform this operation")
        }
    }

    fun ensureOrganizationAccess(organizationId: Long) {
        if (isManager()) {
            return
        }

        if (getCurrentOrganizationId() != organizationId) {
            throw ForbiddenOperationException("You do not have access to this organization")
        }
    }
}
