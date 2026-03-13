package com.mvprestaurante.mvp.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.mvprestaurante.mvp.DTO.UsuarioDTORequest;
import com.mvprestaurante.mvp.models.Usuario;
import com.mvprestaurante.mvp.services.UsuarioService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioServicio;

    @GetMapping("/login")
    public String loginInicioSeccion() {
        return "login";
    }

    @GetMapping("/nuevo")
    public String registroUsuario(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "usuario/registro";
    }

    @PostMapping("/guardar")
    public String guardarUsuario(@Valid UsuarioDTORequest usuario, BindingResult result,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Por favor, corrija los errores en el formulario.");
            return "redirect:/nuevo";
        }
        try {
            usuarioServicio.guardarUsuario(usuario);
            redirectAttributes.addFlashAttribute("success", "Usuario registrado!");
            return "redirect:/nuevo"; // Redirige al mismo formulario

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Ocurrió un error en el registro. Intente nuevamente.");
            return "redirect:/nuevo";
        }

    }
}
