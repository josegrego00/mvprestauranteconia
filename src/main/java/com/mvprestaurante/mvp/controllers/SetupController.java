package com.mvprestaurante.mvp.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mvprestaurante.mvp.models.Empresa;
import com.mvprestaurante.mvp.models.Usuario;
import com.mvprestaurante.mvp.repositories.UsuarioRepositorio;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/setup")
@RequiredArgsConstructor
@Slf4j
public class SetupController {

    private final UsuarioRepositorio usuarioRepositorio;
    private final PasswordEncoder passwordEncoder;

    private static boolean habilitada = true;

    @PostMapping
    public ResponseEntity<?> setup(
            @RequestParam String username,
            @RequestParam String password) {

        if (!habilitada) {
            return ResponseEntity.badRequest().body("Setup deshabilitado");
        }

        if (usuarioRepositorio.existsByEsSuperadminTrue()) {
            habilitada = false;
            return ResponseEntity.badRequest().body("Ya existe un superadmin");
        }

        if (username == null || username.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Username requerido");
        }

        if (password == null || password.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Password requerido");
        }

        Usuario superadmin = Usuario.builder()
                .nombre("Super Admin")
                .nombreUsuario(username)
                .contrasenna(passwordEncoder.encode(password))
                .rol("ADMINDEV")
                .esSuperadmin(true)
                .estaActivo(true)
                .build();

        usuarioRepositorio.save(superadmin);
        habilitada = false;

        log.info("Superadmin creado: {}", username);
        return ResponseEntity.ok("Superadmin creado exitosamente");
    }
}
