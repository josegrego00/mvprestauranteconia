package com.mvprestaurante.mvp.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mvprestaurante.mvp.models.Empresa;
import com.mvprestaurante.mvp.services.EmpresaService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/superadmin")
@RequiredArgsConstructor
public class SuperAdminController {

    private final EmpresaService empresaService;

    @GetMapping("/empresas")
    public String listaEmpresas(Model model) {
        model.addAttribute("empresas", empresaService.listarTodas());
        return "superadmin/empresas/lista";
    }

    @GetMapping("/empresas/nueva")
    public String nuevaEmpresa(Model model) {
        model.addAttribute("empresa", new Empresa());
        return "superadmin/empresas/formulario";
    }

    @PostMapping("/empresas/guardar")
    public String guardarEmpresa(Empresa empresa, RedirectAttributes redirectAttributes) {
        try {
            empresaService.guardar(empresa);
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
    public String actualizarEmpresa(@PathVariable Long id, Empresa empresa, RedirectAttributes redirectAttributes) {
        try {
            empresaService.actualizar(id, empresa);
            redirectAttributes.addFlashAttribute("success", "Empresa actualizada!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/superadmin/empresas";
    }

    @GetMapping("/empresas/activar/{id}")
    public String activarEmpresa(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Empresa empresa = empresaService.buscarPorId(id);
            empresa.setActiva(true);
            empresaService.actualizar(id, empresa);
            redirectAttributes.addFlashAttribute("success", "Empresa activada!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/superadmin/empresas";
    }

    @GetMapping("/empresas/desactivar/{id}")
    public String desactivarEmpresa(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Empresa empresa = empresaService.buscarPorId(id);
            empresa.setActiva(false);
            empresaService.actualizar(id, empresa);
            redirectAttributes.addFlashAttribute("success", "Empresa desactivada!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/superadmin/empresas";
    }
}
