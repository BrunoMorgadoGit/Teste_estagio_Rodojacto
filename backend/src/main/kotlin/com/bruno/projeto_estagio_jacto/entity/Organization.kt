package com.bruno.projeto_estagio_jacto.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime

@Entity
@Table(name = "organizations")
class Organization(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var corporateName: String,

    @Column(nullable = false, unique = true)
    var registrationCode: String,

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null,

    @OneToMany(mappedBy = "organization", fetch = FetchType.LAZY)
    var collaborators: MutableList<Collaborator> = mutableListOf(),

    @OneToMany(mappedBy = "organization", fetch = FetchType.LAZY)
    var devices: MutableList<Device> = mutableListOf(),
)
