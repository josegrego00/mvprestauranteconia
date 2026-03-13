package com.mvprestaurante.mvp.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mvprestaurante.mvp.DTO.EmpresaDTO;

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
            @RequestParam(defaultValue = "basico") String plan,
            @RequestParam(defaultValue = "true") Boolean activa,
            RedirectAttributes redirectAttributes) {

        try {
            // Crear DTO con los datos
            EmpresaDTO dto = new EmpresaDTO();
            dto.setSubdominio(subdominio);
            dto.setNombreEmpresa(nombreEmpresa);
            dto.setEmail(email);
            dto.setTelefono(telefono);
            dto.setPlan(plan);
            dto.setActiva(activa);

            // El servicio maneja la lógica de negocio y retorna DTO de respuesta
            EmpresaDTO empresaCreada = empresaService.registrarEmpresa(dto);

            // Mensaje de éxito
            redirectAttributes.addFlashAttribute("mensaje",
                    "¡Empresa registrada con éxito! Tu subdominio es: " + empresaCreada.getSubdominio());
            redirectAttributes.addFlashAttribute("tipo", "success");

            // Construir URL según el entorno
            String urlLogin = construirUrlLogin(empresaCreada.getSubdominio());
            return "redirect:" + urlLogin;

        } catch (Exception e) {
            // Mensaje de error
            redirectAttributes.addFlashAttribute("mensaje",
                    "Error al registrar la empresa: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipo", "error");

            // Volver al formulario con los datos ingresados
            return "redirect:/registro?subdominio=" + subdominio;
        }
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