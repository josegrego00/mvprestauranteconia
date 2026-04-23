package com.mvprestaurante.mvp.services;

import java.util.List;
import java.util.Optional;

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
import com.mvprestaurante.mvp.utils.AuditLogger;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepositorio usuarioRepositorio;
    private final EmpresaRepositorio empresaRepositorio;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioMapper usuarioMapper;
    private final AuditLogger auditLogger;

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
        auditLogger.logListar("Usuario", usuarios.size());
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
        auditLogger.logBuscar("Usuario", id.toString());
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

        Optional<Usuario> usuarioExistente = usuarioRepositorio.findByNombreUsuarioAndEmpresa_Id(
                usuarioDTO.getNombreUsuario(), empresaId);
        if (usuarioExistente.isPresent()) {
            throw new BusinessException("El nombre de usuario ya existe en esta empresa");
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
        auditLogger.logCrear("Usuario", usuarioGuardado.getId().toString());
        return usuarioMapper.toResponse(usuarioGuardado);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public UsuarioResponseDTO actualizarUsuario(Long id, UsuarioRequestDTO usuarioDTO) {
        validarTenant();
        Long empresaId = TenantContext.getTenantId();

        Usuario usuarioExistente = usuarioRepositorio.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));

        if (!usuarioExistente.getNombreUsuario().equals(usuarioDTO.getNombreUsuario())) {
            Optional<Usuario> usuarioDuplicado = usuarioRepositorio.findByNombreUsuarioAndEmpresa_Id(
                    usuarioDTO.getNombreUsuario(), empresaId);
            if (usuarioDuplicado.isPresent()) {
                throw new BusinessException("El nombre de usuario ya existe en esta empresa");
            }
        }

        usuarioExistente.setNombre(usuarioDTO.getNombre());
        usuarioExistente.setNombreUsuario(usuarioDTO.getNombreUsuario());
        usuarioExistente.setEmail(usuarioDTO.getEmail());
        usuarioExistente.setRol(usuarioDTO.getRol());

        if (usuarioDTO.getContrasenna() != null && !usuarioDTO.getContrasenna().isEmpty()) {
            usuarioExistente.setContrasenna(passwordEncoder.encode(usuarioDTO.getContrasenna()));
        }

        Usuario usuarioActualizado = usuarioRepositorio.save(usuarioExistente);
        auditLogger.logActualizar("Usuario", id.toString());
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
        auditLogger.logDesactivar("Usuario", id.toString());
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
        auditLogger.logActivar("Usuario", id.toString());
        return usuarioMapper.toResponse(usuarioGuardado);
    }

    // ----------------------------- esto es para crear el usuario admin por defecto
    // al crear la empresa -----------------------------

    @Transactional
    @PreAuthorize("hasRole('ADMINDEV')")
    public void crearUsuarioAdmin(Empresa empresa) {
        validarTenant();
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

}