package com.mvprestaurante.mvp.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mvprestaurante.mvp.DTO.EmpresaDTO;

import com.mvprestaurante.mvp.models.Empresa;
import com.mvprestaurante.mvp.services.EmpresaService;
import com.mvprestaurante.mvp.services.UsuarioService;

@Controller
@RequestMapping("/empresa")
public class EmpresaController {

    @Autowired
    private EmpresaService empresaService;

    @Value("${app.dominio.principal:localhost}")
    private String dominioPrincipal;

    @Value("${app.dominio.produccion:mibombay.com}")
    private String dominioProduccion;

    @Value("${app.entorno:desarrollo}")
    private String entorno;

    @PostMapping("/guardar")
    public String guardarEmpresa(
            @RequestParam String subdominio,
            @RequestParam String nombreEmpresa,
            @RequestParam String email,
            @RequestParam(required = false) String telefono,
            @RequestParam(defaultValue = "BASIC") String plan,
            RedirectAttributes redirectAttributes) {

        try {
            EmpresaDTO dto = new EmpresaDTO();
            dto.setSubdominio(subdominio);
            dto.setNombreEmpresa(nombreEmpresa);
            dto.setEmail(email);
            dto.setTelefono(telefono);
            dto.setPlan(plan);
            dto.setActiva(false);

            EmpresaDTO empresaCreada = empresaService.registrarEmpresa(dto);

            redirectAttributes.addFlashAttribute("empresa", empresaCreada);
            return "redirect:/empresa/espera-activacion";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje",
                    "Error al registrar la empresa: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipo", "error");
            return "redirect:/registro?subdominio=" + subdominio;
        }
    }

    @GetMapping("/espera-activacion")
    public String esperaActivacion(@RequestParam(required = false) String subdominio, Model model) {
        if (subdominio != null && !subdominio.isEmpty()) {
            Empresa empresa = empresaService.buscarPorSubdominio(subdominio);
            if (empresa != null) {
                model.addAttribute("empresa", empresa);
            }
        }
        return "empresa/espera-activacion";
    }

    private String construirUrlLogin(String subdominio) {
        String url;

        if ("produccion".equalsIgnoreCase(entorno)) {
            // Entorno de producción
            url = "https://" + subdominio + "." + dominioProduccion + "/login";
        } else {
            // Entorno de desarrollo (localhost)
            url = "http://" + subdominio + "." + dominioPrincipal + ":8080/login";
        }

        return url;
    }
}