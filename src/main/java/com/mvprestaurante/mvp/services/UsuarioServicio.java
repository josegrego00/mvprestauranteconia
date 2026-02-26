package com.mvprestaurante.mvp.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.mvprestaurante.mvp.DTO.UsuarioDTORequest;
import com.mvprestaurante.mvp.mapper.UsuarioMapper;
import com.mvprestaurante.mvp.models.Usuario;
import com.mvprestaurante.mvp.repositories.UsuarioRepositorio;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioServicio {

    private final UsuarioRepositorio usuarioRepositorio;

    private final PasswordEncoder passwordEncoder;

    private final UsuarioMapper usuarioMapper;

    public void guardarUsuario(UsuarioDTORequest usuario) {

        // Encriptar la contraseña antes de guardar
        String contraseñaEncriptada = passwordEncoder.encode(usuario.getContrasenna());
        usuario.setContrasenna(contraseñaEncriptada);
        if(usuario.getRol().equalsIgnoreCase("administrador")) {
            usuario.setRol("ADMIN"); 
        }else{
            usuario.setRol("CAJERO");
        }
        usuarioRepositorio.save(usuarioMapper.toEntity(usuario));

    }

}
