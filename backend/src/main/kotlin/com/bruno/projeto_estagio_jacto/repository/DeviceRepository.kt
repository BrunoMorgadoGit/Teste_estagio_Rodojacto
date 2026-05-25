package com.bruno.projeto_estagio_jacto.repository

import com.bruno.projeto_estagio_jacto.dto.dashboard.OrganizationMetricResponse
import com.bruno.projeto_estagio_jacto.entity.Device
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface DeviceRepository : JpaRepository<Device, Long> {
    fun existsByAssetTag(assetTag: String): Boolean
    fun findAllByOrganizationId(organizationId: Long): List<Device>
    fun countByOrganizationId(organizationId: Long): Long

    @Query(
        """
        select new com.bruno.projeto_estagio_jacto.dto.dashboard.OrganizationMetricResponse(
            o.id,
            o.corporateName,
            count(d.id)
        )
        from Device d
        join d.organization o
        group by o.id, o.corporateName
        order by o.corporateName
        """,
    )
    fun countDevicesByOrganization(): List<OrganizationMetricResponse>
}
