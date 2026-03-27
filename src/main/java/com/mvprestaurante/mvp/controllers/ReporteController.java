package com.mvprestaurante.mvp.controllers;

import com.mvprestaurante.mvp.DTO.ReporteDashboardDTO;
import com.mvprestaurante.mvp.services.ReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final ReporteService reporteService;

    @GetMapping
    public String dashboard(Model model) {
        ReporteDashboardDTO dashboard = reporteService.obtenerDashboard();
        model.addAttribute("dashboard", dashboard);
        return "reportes/dashboard";
    }
}
