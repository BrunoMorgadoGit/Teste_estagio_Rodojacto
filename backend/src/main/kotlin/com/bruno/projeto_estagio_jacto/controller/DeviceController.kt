package com.bruno.projeto_estagio_jacto.controller

import com.bruno.projeto_estagio_jacto.dto.device.DeviceRequest
import com.bruno.projeto_estagio_jacto.dto.device.DeviceResponse
import com.bruno.projeto_estagio_jacto.service.DeviceService
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
@RequestMapping("/api/devices")
class DeviceController(
    private val deviceService: DeviceService,
) {

    @GetMapping
    fun findAll(@RequestParam(required = false) organizationId: Long?): List<DeviceResponse> =
        organizationId?.let(deviceService::findAllByOrganization) ?: deviceService.findAll()

    @GetMapping("/{id}")
    fun findById(@PathVariable id: Long): DeviceResponse = deviceService.findById(id)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody request: DeviceRequest): DeviceResponse =
        deviceService.create(request)

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @Valid @RequestBody request: DeviceRequest,
    ): DeviceResponse = deviceService.update(id, request)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long) {
        deviceService.delete(id)
    }
}
