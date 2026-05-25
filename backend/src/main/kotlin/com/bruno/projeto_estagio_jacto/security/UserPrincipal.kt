package com.bruno.projeto_estagio_jacto.security

import com.bruno.projeto_estagio_jacto.entity.Collaborator
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class UserPrincipal(
    private val collaborator: Collaborator,
) : UserDetails {
    val collaboratorId: Long
        get() = collaborator.id ?: error("Collaborator id should not be null")

    val organizationId: Long
        get() = collaborator.organization.id ?: error("Organization id should not be null")

    val accessLevel = collaborator.accessLevel

    override fun getAuthorities(): Collection<GrantedAuthority> =
        listOf(SimpleGrantedAuthority("ROLE_${collaborator.accessLevel.name}"))

    override fun getPassword(): String = collaborator.password

    override fun getUsername(): String = collaborator.email

    companion object {
        fun fromAuthentication(authentication: Authentication): UserPrincipal =
            authentication.principal as? UserPrincipal
                ?: error("Authenticated principal is not a UserPrincipal")
    }
}
