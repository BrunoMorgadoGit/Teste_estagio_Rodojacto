package com.bruno.projeto_estagio_jacto.controller

import com.bruno.projeto_estagio_jacto.dto.organization.OrganizationRequest
import com.bruno.projeto_estagio_jacto.dto.organization.OrganizationResponse
import com.bruno.projeto_estagio_jacto.service.OrganizationService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/organizations")
class OrganizationController(
    private val organizationService: OrganizationService,
) {

    @GetMapping
    fun findAll(): List<OrganizationResponse> = organizationService.findAll()

    @GetMapping("/{id}")
    fun findById(@PathVariable id: Long): OrganizationResponse = organizationService.findById(id)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody request: OrganizationRequest): OrganizationResponse =
        organizationService.create(request)

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @Valid @RequestBody request: OrganizationRequest,
    ): OrganizationResponse = organizationService.update(id, request)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long) {
        organizationService.delete(id)
    }
}
