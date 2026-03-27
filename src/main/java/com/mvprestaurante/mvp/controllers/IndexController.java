package com.mvprestaurante.mvp.controllers;

import com.mvprestaurante.mvp.DTO.ReporteDashboardDTO;
import com.mvprestaurante.mvp.services.ReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class IndexController {

    private final ReporteService reporteService;

    @GetMapping("/")
    public String index() {
        return "inicio";
    }

    @GetMapping("dashboard")
    public String dashboard(Model model) {
        try {
            ReporteDashboardDTO dashboard = reporteService.obtenerDashboard();
            model.addAttribute("dashboard", dashboard);
        } catch (Exception e) {
        }
        return "index";
    }

    @GetMapping("registro")
    public String registro(@RequestParam(required = false) String subdominio, Model model) {
        if (subdominio != null && !subdominio.isEmpty()) {
            model.addAttribute("subdominio", subdominio);
        }
        return "registro";
    }
}
