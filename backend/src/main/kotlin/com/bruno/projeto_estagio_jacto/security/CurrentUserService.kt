package com.bruno.projeto_estagio_jacto.security

import com.bruno.projeto_estagio_jacto.entity.AccessLevel
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

@Service
class CurrentUserService {

    fun getCurrentUser(): UserPrincipal {
        val authentication = SecurityContextHolder.getContext().authentication
            ?: throw AccessDeniedException("Authentication is required")

        if (!authentication.isAuthenticated) {
            throw AccessDeniedException("Authentication is required")
        }

        return UserPrincipal.fromAuthentication(authentication)
    }

    fun isManager(): Boolean = getCurrentUser().accessLevel == AccessLevel.MANAGER

    fun getCurrentOrganizationId(): Long = getCurrentUser().organizationId

    fun ensureOrganizationAccess(organizationId: Long) {
        if (isManager()) {
            return
        }

        if (getCurrentOrganizationId() != organizationId) {
            throw AccessDeniedException("You do not have access to this organization")
        }
    }
}
