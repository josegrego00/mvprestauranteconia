package com.mvprestaurante.mvp.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.mvprestaurante.mvp.models.Usuario;
import com.mvprestaurante.mvp.repositories.UsuarioRepositorio;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepositorio repositorio;

    public CustomUserDetailsService(UsuarioRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Usuario usuario = repositorio.findBynombreUsuario(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        return new CustomUserDetails(usuario);
    }

}
