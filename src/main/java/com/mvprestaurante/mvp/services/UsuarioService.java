package com.mvprestaurante.mvp.services;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.mvprestaurante.mvp.DTO.UsuarioRequestDTO;
import com.mvprestaurante.mvp.DTO.UsuarioResponseDTO;
import com.mvprestaurante.mvp.exceptions.BusinessException;
import com.mvprestaurante.mvp.mapper.UsuarioMapper;
import com.mvprestaurante.mvp.models.Empresa;
import com.mvprestaurante.mvp.models.Usuario;
import com.mvprestaurante.mvp.multitenant.TenantContext;
import com.mvprestaurante.mvp.repositories.EmpresaRepositorio;
import com.mvprestaurante.mvp.repositories.UsuarioRepositorio;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepositorio usuarioRepositorio;
    private final EmpresaRepositorio empresaRepositorio;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioMapper usuarioMapper;

    private void validarTenant() {
        Long empresaId = TenantContext.getTenantId();
        if (empresaId == null) {
            throw new BusinessException("No se ha identificado la empresa");
        }
    }

    private void validarUsuarioActivo(Usuario usuario) {
        if (usuario == null) {
            throw new BusinessException("Usuario no encontrado");
        }
        if (usuario.getEstaActivo() == null || !usuario.getEstaActivo()) {
            throw new BusinessException("El usuario está inactivo");
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ADMINDEV')")
    public List<UsuarioResponseDTO> listarUsuarios() {
        validarTenant();
        Long empresaId = TenantContext.getTenantId();
        List<Usuario> usuarios = usuarioRepositorio.findByEmpresaId(empresaId);
        return usuarios.stream()
                .map(usuarioMapper::toResponse)
                .toList();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ADMINDEV')")
    public UsuarioResponseDTO buscarPorId(Long id) {
        validarTenant();
        Long empresaId = TenantContext.getTenantId();
        Usuario usuario = usuarioRepositorio.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));
        return usuarioMapper.toResponse(usuario);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public UsuarioResponseDTO guardarUsuario(UsuarioRequestDTO usuarioDTO) {
        validarTenant();
        Long empresaId = TenantContext.getTenantId();

        if (!usuarioDTO.getRol().equals("ADMIN") && !usuarioDTO.getRol().equals("CAJERO")) {
            throw new BusinessException("Rol inválido. Solo se permiten ADMIN o CAJERO");
        }

        Empresa empresa = empresaRepositorio.findById(empresaId)
                .orElseThrow(() -> new BusinessException("Empresa no encontrada"));

        String contraseñaEncriptada = passwordEncoder.encode(usuarioDTO.getContrasenna());
        usuarioDTO.setContrasenna(contraseñaEncriptada);

        Usuario entidad = usuarioMapper.toEntity(usuarioDTO);
        entidad.setEmpresa(empresa);
        entidad.setEsSuperadmin(false);
        entidad.setEstaActivo(true);

        Usuario usuarioGuardado = usuarioRepositorio.save(entidad);
        return usuarioMapper.toResponse(usuarioGuardado);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public UsuarioResponseDTO actualizarUsuario(Long id, UsuarioRequestDTO usuarioDTO) {
        validarTenant();
        Long empresaId = TenantContext.getTenantId();

        Usuario usuarioExistente = usuarioRepositorio.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));

        usuarioExistente.setNombre(usuarioDTO.getNombre());
        usuarioExistente.setNombreUsuario(usuarioDTO.getNombreUsuario());
        usuarioExistente.setEmail(usuarioDTO.getEmail());
        usuarioExistente.setRol(usuarioDTO.getRol());

        if (usuarioDTO.getContrasenna() != null && !usuarioDTO.getContrasenna().isEmpty()) {
            usuarioExistente.setContrasenna(passwordEncoder.encode(usuarioDTO.getContrasenna()));
        }

        Usuario usuarioActualizado = usuarioRepositorio.save(usuarioExistente);
        return usuarioMapper.toResponse(usuarioActualizado);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public UsuarioResponseDTO eliminarUsuario(Long id) {
        validarTenant();
        Long empresaId = TenantContext.getTenantId();

        Usuario usuario = usuarioRepositorio.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));

        if (usuario.getEstaActivo() == null || !usuario.getEstaActivo()) {
            throw new BusinessException("El usuario ya está inactivo");
        }

        usuario.setEstaActivo(false);
        Usuario usuarioGuardado = usuarioRepositorio.save(usuario);
        return usuarioMapper.toResponse(usuarioGuardado);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public UsuarioResponseDTO activarUsuario(Long id) {
        validarTenant();

        Usuario usuario = usuarioRepositorio.findByIdAndEmpresaId(id, TenantContext.getTenantId())
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));

        if (usuario.getEstaActivo() != null && usuario.getEstaActivo()) {
            throw new BusinessException("El usuario ya está activo");
        }

        usuario.setEstaActivo(true);
        Usuario usuarioGuardado = usuarioRepositorio.save(usuario);
        return usuarioMapper.toResponse(usuarioGuardado);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMINDEV')")
    public void crearUsuarioAdmin(Empresa empresa) {
        Long tenantId = empresa.getId();

        if (tenantId == null) {
            throw new BusinessException("No hay tenant en el contexto");
        }

        Usuario admin = Usuario.builder()
                .nombre("admin")
                .nombreUsuario("admin")
                .contrasenna(passwordEncoder.encode("123456"))
                .rol("ADMIN")
                .email("admin@" + empresa.getSubdominio() + ".com")
                .empresa(empresa)
                .estaActivo(true)
                .esSuperadmin(false)
                .build();

        usuarioRepositorio.save(admin);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMINDEV')")
    public void crearUsuarioAdmin(Long empresaId) {
        Empresa empresa = empresaRepositorio.findById(empresaId)
                .orElseThrow(() -> new BusinessException("Empresa no encontrada"));
        crearUsuarioAdmin(empresa);
    }
}