package com.bruno.projeto_estagio_jacto.security

import com.bruno.projeto_estagio_jacto.repository.CollaboratorRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
    private val collaboratorRepository: CollaboratorRepository,
) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        val collaborator = collaboratorRepository.findByEmail(username)
            ?: throw UsernameNotFoundException("Collaborator with email $username not found")

        return UserPrincipal(collaborator)
    }
}
