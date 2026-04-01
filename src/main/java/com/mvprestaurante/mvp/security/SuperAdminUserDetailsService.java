package com.mvprestaurante.mvp.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.mvprestaurante.mvp.models.Usuario;
import com.mvprestaurante.mvp.repositories.UsuarioRepositorio;

@Service
public class SuperAdminUserDetailsService implements UserDetailsService {

    private final UsuarioRepositorio repositorio;

    public SuperAdminUserDetailsService(UsuarioRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = repositorio.findBynombreUsuario(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        if (usuario.getRol() == null || !"ADMINDEV".equals(usuario.getRol())) {
            throw new UsernameNotFoundException("Usuario no es ADMINDEV: " + username);
        }

        return new CustomUserDetails(usuario);
    }
}
