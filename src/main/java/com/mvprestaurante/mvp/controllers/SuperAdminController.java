package com.mvprestaurante.mvp.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mvprestaurante.mvp.DTO.EmpresaDTO;
import com.mvprestaurante.mvp.mapper.EmpresaMapperImpl;
import com.mvprestaurante.mvp.models.Empresa;
import com.mvprestaurante.mvp.models.Usuario;
import com.mvprestaurante.mvp.repositories.UsuarioRepositorio;
import com.mvprestaurante.mvp.services.EmpresaService;
import com.mvprestaurante.mvp.services.UsuarioService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/superadmin")
@RequiredArgsConstructor
public class SuperAdminController {

    private final EmpresaMapperImpl empresaMapperImpl;
    private final EmpresaService empresaService;
    private final UsuarioRepositorio usuarioRepositorio;
    private final UsuarioService usuarioService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/empresas")
    public String listaEmpresas(Model model) {
        model.addAttribute("empresas", empresaService.listarTodas());
        return "superadmin/empresas/lista";
    }

    @PostMapping("/cambiar-password")
    public String cambiarPassword(
            @RequestParam String passwordActual,
            @RequestParam String nuevaPassword,
            @RequestParam String confirmarPassword,
            Authentication authentication,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        String username = authentication.getName();
        Usuario usuario = usuarioRepositorio.findBynombreUsuario(username)
                .orElse(null);

        if (usuario == null || !"ADMINDEV".equals(usuario.getRol())) {
            redirectAttributes.addFlashAttribute("error", "Usuario no encontrado");
            return "redirect:/superadmin/empresas";
        }

        if (!passwordEncoder.matches(passwordActual, usuario.getContrasenna())) {
            redirectAttributes.addFlashAttribute("error", "La contraseña actual es incorrecta");
            return "redirect:/superadmin/empresas";
        }

        if (!nuevaPassword.equals(confirmarPassword)) {
            redirectAttributes.addFlashAttribute("error", "Las contraseñas no coinciden");
            return "redirect:/superadmin/empresas";
        }

        if (nuevaPassword.length() < 6) {
            redirectAttributes.addFlashAttribute("error", "La contraseña debe tener al menos 6 caracteres");
            return "redirect:/superadmin/empresas";
        }

        usuario.setContrasenna(passwordEncoder.encode(nuevaPassword));
        usuarioRepositorio.save(usuario);

        redirectAttributes.addFlashAttribute("success", "Contraseña cambiada. Por favor inicie sesión con la nueva contraseña");
        
        request.getSession().invalidate();
        return "redirect:/superadmin/login?passwordChanged=true";
    }

    @GetMapping("/empresas/nueva")
    public String nuevaEmpresa(Model model) {
        model.addAttribute("empresa", new EmpresaDTO());
        return "superadmin/empresas/formulario";
    }

    @PostMapping("/empresas/guardar")
    public String guardarEmpresa(EmpresaDTO empresaDTO, RedirectAttributes redirectAttributes) {
        try {
            empresaService.guardar(empresaDTO);
            redirectAttributes.addFlashAttribute("success", "Empresa creada exitosamente!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/superadmin/empresas";
    }

    @GetMapping("/empresas/editar/{id}")
    public String editarEmpresa(@PathVariable Long id, Model model) {
        model.addAttribute("empresa", empresaService.buscarPorId(id));
        return "superadmin/empresas/formulario";
    }

    @PostMapping("/empresas/actualizar/{id}")
    public String actualizarEmpresa(@PathVariable Long id, EmpresaDTO empresaDTO, RedirectAttributes redirectAttributes) {
        try {
            empresaService.actualizar(id, empresaDTO);
            redirectAttributes.addFlashAttribute("success", "Empresa actualizada!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/superadmin/empresas";
    }

    @GetMapping("/empresas/activar/{id}")
    public String activarEmpresa(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            empresaService.actualizarEstadoActivo(id, true);
            EmpresaDTO empresaDTO = empresaService.buscarPorId(id);
            Empresa empresa = Empresa.builder().id(id).nombreEmpresa(empresaDTO.getNombreEmpresa()).build();
            usuarioService.crearUsuarioAdmin(empresa);
            redirectAttributes.addFlashAttribute("success", "Empresa activada! Se ha creado el usuario admin.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/superadmin/empresas";
    }

    @GetMapping("/empresas/desactivar/{id}")
    public String desactivarEmpresa(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            empresaService.actualizarEstadoActivo(id, false);
            redirectAttributes.addFlashAttribute("success", "Empresa desactivada!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/superadmin/empresas";
    }
}