package com.bruno.projeto_estagio_jacto.repository

import com.bruno.projeto_estagio_jacto.dto.dashboard.OrganizationMetricResponse
import com.bruno.projeto_estagio_jacto.entity.AccessLevel
import com.bruno.projeto_estagio_jacto.entity.Collaborator
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface CollaboratorRepository : JpaRepository<Collaborator, Long> {
    fun existsByEmail(email: String): Boolean
    fun findByEmail(email: String): Collaborator?
    fun findAllByOrganizationId(organizationId: Long): List<Collaborator>
    fun countByAccessLevel(accessLevel: AccessLevel): Long
    fun countByOrganizationId(organizationId: Long): Long
    fun countByOrganizationIdAndAccessLevel(organizationId: Long, accessLevel: AccessLevel): Long

    @Query(
        """
        select new com.bruno.projeto_estagio_jacto.dto.dashboard.OrganizationMetricResponse(
            o.id,
            o.corporateName,
            count(c.id)
        )
        from Collaborator c
        join c.organization o
        group by o.id, o.corporateName
        order by o.corporateName
        """,
    )
    fun countCollaboratorsByOrganization(): List<OrganizationMetricResponse>
}
