package com.bruno.projeto_estagio_jacto.controller

import com.bruno.projeto_estagio_jacto.dto.dashboard.DashboardSummaryResponse
import com.bruno.projeto_estagio_jacto.service.DashboardService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/dashboard")
class DashboardController(
    private val dashboardService: DashboardService,
) {

    @GetMapping("/summary")
    fun getSummary(): DashboardSummaryResponse = dashboardService.getSummary()
}
