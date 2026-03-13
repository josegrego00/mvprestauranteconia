package com.mvprestaurante.mvp.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class IndexController {
    @GetMapping("/")
    public String index() {
        return "inicio";
    }

    @GetMapping("dashboard")
    public String dashboard() {
        return "index";
    }

    @GetMapping("registro")
    public String registro(@RequestParam(required = false) String subdominio, Model model) {
        // Si viene un subdominio por parámetro, lo pasamos a la vista
        if (subdominio != null && !subdominio.isEmpty()) {
            model.addAttribute("subdominio", subdominio);
        }
        return "registro"; // Este es tu nuevo archivo HTML
    }
}
