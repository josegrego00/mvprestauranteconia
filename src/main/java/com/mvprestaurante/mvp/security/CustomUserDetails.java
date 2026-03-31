package com.mvprestaurante.mvp.security;

import java.util.Collection;
import java.util.List;

import org.springframework.lang.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.mvprestaurante.mvp.models.Usuario;

public class CustomUserDetails implements UserDetails {

    private String nombreUsuario;
    private Long id;
    private String contrasenna;
    private String rol;
    private String email;
    private boolean activo;

    public CustomUserDetails(Usuario usuario) {
        this.nombreUsuario = usuario.getNombreUsuario();
        this.id = usuario.getId();
        this.contrasenna = usuario.getContrasenna();
        this.email = usuario.getEmail();
        this.rol = usuario.getRol();
        this.activo = usuario.getEstaActivo() != null && usuario.getEstaActivo();
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getRol() {
        return rol;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + rol));
    }

    @Override
    public @Nullable String getPassword() {
        return contrasenna;
    }

    @Override
    public String getUsername() {
        return nombreUsuario;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return activo;
    }

}
