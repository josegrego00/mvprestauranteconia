package com.mvprestaurante.mvp.controllers;

import com.mvprestaurante.mvp.DTO.ReporteDashboardDTO;
import com.mvprestaurante.mvp.multitenant.TenantContext;
import com.mvprestaurante.mvp.services.ReporteService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class IndexController {

    private static final Logger log = LoggerFactory.getLogger(IndexController.class);
    private final ReporteService reporteService;

    @GetMapping("/")
    public String index() {
        log.info("=== INDEX PAGE ===");
        log.info("TenantContext actual: {}", TenantContext.getTenantId());
        return "inicio";
    }

    @GetMapping("/login")
    public String login() {
        log.info("=== LOGIN PAGE ===");
        log.info("TenantContext actual: {}", TenantContext.getTenantId());
        return "login";
    }

    @GetMapping("/salir")
    public String salir(HttpServletRequest request) {
        log.info("=== SALIR METHOD ===");

        TenantContext.clear();
        log.info("TenantContext after clear: {}", TenantContext.getTenantId());

        return "redirect://localhost:8080/"; // Redirige al inicio después de salir
    }

    @GetMapping("/superadmin/login")
    public String loginSuperadmin() {
        return "login-superadmin";
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
