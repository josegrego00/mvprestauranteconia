package com.mvprestaurante.mvp.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mvprestaurante.mvp.DTO.UsuarioRequestDTO;
import com.mvprestaurante.mvp.DTO.UsuarioResponseDTO;
import com.mvprestaurante.mvp.services.UsuarioService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/usuarios")
public class UsuarioController {

    private static final Logger log = LoggerFactory.getLogger(UsuarioController.class);
    private final UsuarioService usuarioService;

    @GetMapping
    public String listaUsuarios(Model model) {
        model.addAttribute("usuarios", usuarioService.listarUsuarios());
        return "usuario/lista";
    }

    @GetMapping("/nuevo")
    public String nuevoUsuario(Model model) {
        if (!model.containsAttribute("usuarioDTO")) {
            model.addAttribute("usuarioDTO", new UsuarioRequestDTO());
        }
        model.addAttribute("isEdit", false);
        return "usuario/formulario";
    }

    @GetMapping("/editar/{id}")
    public String editarUsuario(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            UsuarioResponseDTO usuario = usuarioService.buscarPorId(id);
            model.addAttribute("usuarioDTO", usuario);
            model.addAttribute("isEdit", true);
            return "usuario/formulario";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Usuario no encontrado");
            return "redirect:/usuarios";
        }
    }

    @GetMapping("/ver/{id}")
    public String verUsuario(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            UsuarioResponseDTO usuario = usuarioService.buscarPorId(id);
            model.addAttribute("usuarioDTO", usuario);
            return "usuario/ver";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Usuario no encontrado");
            return "redirect:/usuarios";
        }
    }

    @PostMapping("/guardar")
    public String guardarUsuario(@Valid @ModelAttribute UsuarioRequestDTO usuarioDTO,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        log.info("=== guardarUsuario called ===");
        log.info("usuarioDTO: {}", usuarioDTO);

        if (bindingResult.hasErrors()) {
            log.warn("=== BindingResult has errors ===");
            bindingResult.getFieldErrors().forEach(error -> 
                log.warn("Field error: {} - {}", error.getField(), error.getDefaultMessage()));
            model.addAttribute("error", "Error de validación en los datos ingresados");
            model.addAttribute("isEdit", false);
            model.addAttribute("usuarioDTO", usuarioDTO);
            return "usuario/formulario";
        }

        try {
            log.info("Attempting to save user...");
            usuarioService.guardarUsuario(usuarioDTO);
            log.info("User saved successfully!");
            redirectAttributes.addFlashAttribute("success", "Usuario guardado exitosamente!");
            return "redirect:/usuarios";

        } catch (Exception e) {
            log.error("Error saving user: ", e);
            model.addAttribute("error", "Ocurrió un error: " + e.getMessage());
            model.addAttribute("isEdit", false);
            model.addAttribute("usuarioDTO", usuarioDTO);
            return "usuario/formulario";
        }
    }

    @PostMapping("/actualizar/{id}")
    public String actualizarUsuario(@PathVariable Long id,
            @ModelAttribute UsuarioRequestDTO usuarioDTO,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("error", "Error de validación en los datos ingresados");
            model.addAttribute("isEdit", true);
            model.addAttribute("usuarioDTO", usuarioDTO);
            return "usuario/formulario";
        }

        try {
            usuarioService.actualizarUsuario(id, usuarioDTO);
            redirectAttributes.addFlashAttribute("success", "Usuario actualizado exitosamente!");
            return "redirect:/usuarios";

        } catch (Exception e) {
            model.addAttribute("error", "Ocurrió un error: " + e.getMessage());
            model.addAttribute("isEdit", true);
            model.addAttribute("usuarioDTO", usuarioDTO);
            return "redirect:/usuarios/editar/" + id;
        }
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.eliminarUsuario(id);
            redirectAttributes.addFlashAttribute("success", "Usuario eliminado exitosamente!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/usuarios";
    }

    @GetMapping("/activar/{id}")
    public String activarUsuario(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.activarUsuario(id);
            redirectAttributes.addFlashAttribute("success", "Usuario activado exitosamente!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/usuarios";
    }
}