package com.bruno.projeto_estagio_jacto.repository

import com.bruno.projeto_estagio_jacto.entity.Organization
import org.springframework.data.jpa.repository.JpaRepository

interface OrganizationRepository : JpaRepository<Organization, Long> {
    fun existsByRegistrationCode(registrationCode: String): Boolean
}
