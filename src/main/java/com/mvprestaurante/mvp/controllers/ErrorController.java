package com.mvprestaurante.mvp.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ErrorController {

    @GetMapping("/error/subdominio-no-encontrado")
    public String subdominioNoEncontrado(@RequestParam String subdominio, Model model) {
        model.addAttribute("subdominio", subdominio);
        return "error/subdominio-no-encontrado";
    }
}
