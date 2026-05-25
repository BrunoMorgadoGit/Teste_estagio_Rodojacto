package com.bruno.projeto_estagio_jacto.controller

import com.bruno.projeto_estagio_jacto.dto.collaborator.CollaboratorRequest
import com.bruno.projeto_estagio_jacto.dto.collaborator.CollaboratorResponse
import com.bruno.projeto_estagio_jacto.service.CollaboratorService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/collaborators")
class CollaboratorController(
    private val collaboratorService: CollaboratorService,
) {

    @GetMapping
    fun findAll(@RequestParam(required = false) organizationId: Long?): List<CollaboratorResponse> =
        organizationId?.let(collaboratorService::findAllByOrganization) ?: collaboratorService.findAll()

    @GetMapping("/{id}")
    fun findById(@PathVariable id: Long): CollaboratorResponse = collaboratorService.findById(id)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody request: CollaboratorRequest): CollaboratorResponse =
        collaboratorService.create(request)

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @Valid @RequestBody request: CollaboratorRequest,
    ): CollaboratorResponse = collaboratorService.update(id, request)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long) {
        collaboratorService.delete(id)
    }
}
