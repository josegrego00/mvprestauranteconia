package com.mvprestaurante.mvp.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.mvprestaurante.mvp.models.Usuario;
import com.mvprestaurante.mvp.multitenant.TenantContext;
import com.mvprestaurante.mvp.repositories.UsuarioRepositorio;

@Service
public class NormalUserDetailsService implements UserDetailsService {

    private final UsuarioRepositorio repositorio;

    public NormalUserDetailsService(UsuarioRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Long empresaId = TenantContext.getTenantId();

        if (empresaId == null) {
            throw new UsernameNotFoundException("No se ha identificado la empresa");
        }

        Usuario usuario = repositorio.findByNombreUsuarioAndEmpresa_Id(username, empresaId)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        if ("ADMINDEV".equals(usuario.getRol())) {
            throw new UsernameNotFoundException("Usuario no válido para esta empresa");
        }

        return new CustomUserDetails(usuario);
    }
}
