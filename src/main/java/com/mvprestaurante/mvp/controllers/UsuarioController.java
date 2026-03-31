package com.mvprestaurante.mvp.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mvprestaurante.mvp.DTO.UsuarioDTORequest;
import com.mvprestaurante.mvp.models.Usuario;
import com.mvprestaurante.mvp.services.UsuarioService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioServicio;

    @GetMapping
    public String listaUsuarios(Model model) {
        model.addAttribute("usuarios", usuarioServicio.listarUsuarios());
        return "usuario/lista";
    }

    @GetMapping("/nuevo")
    public String nuevoUsuario(Model model) {
        if (!model.containsAttribute("usuario")) {
            Usuario nuevoUsuario = new Usuario();
            nuevoUsuario.setEstaActivo(true);
            model.addAttribute("usuario", nuevoUsuario);
        }
        return "usuario/formulario";
    }

    @GetMapping("/editar/{id}")
    public String editarUsuario(@PathVariable Long id, Model model) {
        Usuario usuario = usuarioServicio.buscarPorId(id);
        model.addAttribute("usuario", usuario);
        return "usuario/formulario";
    }

    @GetMapping("/ver/{id}")
    public String verUsuario(@PathVariable Long id, Model model) {
        Usuario usuario = usuarioServicio.buscarPorId(id);
        model.addAttribute("usuario", usuario);
        return "usuario/ver";
    }

    @PostMapping("/guardar")
    public String guardarUsuario(@ModelAttribute Usuario usuario, Model model,
            RedirectAttributes redirectAttributes) {

        try {
            UsuarioDTORequest dto = new UsuarioDTORequest();
            dto.setNombre(usuario.getNombre());
            dto.setNombreUsuario(usuario.getNombreUsuario());
            dto.setContrasenna(usuario.getContrasenna());
            dto.setEmail(usuario.getEmail());
            dto.setRol(usuario.getRol());
            dto.setEstaActivo(usuario.getEstaActivo());
            
            usuarioServicio.guardarUsuario(dto);
            redirectAttributes.addFlashAttribute("success", "Usuario guardado exitosamente!");
            return "redirect:/usuarios";

        } catch (Exception e) {
            model.addAttribute("usuario", usuario);
            model.addAttribute("error", "Ocurrió un error: " + e.getMessage());
            return "usuario/formulario";
        }
    }

    @PostMapping("/actualizar/{id}")
    public String actualizarUsuario(@PathVariable Long id, @ModelAttribute Usuario usuario, Model model,
            RedirectAttributes redirectAttributes) {

        try {
            UsuarioDTORequest dto = new UsuarioDTORequest();
            dto.setNombre(usuario.getNombre());
            dto.setNombreUsuario(usuario.getNombreUsuario());
            dto.setContrasenna(usuario.getContrasenna());
            dto.setEmail(usuario.getEmail());
            dto.setRol(usuario.getRol());
            dto.setEstaActivo(usuario.getEstaActivo());
            
            usuarioServicio.actualizarUsuario(id, dto);
            redirectAttributes.addFlashAttribute("success", "Usuario actualizado exitosamente!");
            return "redirect:/usuarios";

        } catch (Exception e) {
            model.addAttribute("usuario", usuario);
            model.addAttribute("error", "Ocurrió un error: " + e.getMessage());
            return "usuario/formulario";
        }
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            usuarioServicio.eliminarUsuario(id);
            redirectAttributes.addFlashAttribute("success", "Usuario eliminado exitosamente!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ocurrió un error: " + e.getMessage());
        }
        return "redirect:/usuarios";
    }
}
