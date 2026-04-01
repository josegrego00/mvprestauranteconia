package com.mvprestaurante.mvp.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.mvprestaurante.mvp.DTO.UsuarioDTORequest;
import com.mvprestaurante.mvp.exceptions.BusinessException;
import com.mvprestaurante.mvp.mapper.EmpresaMapper;
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

    @Autowired
    private EmpresaRepositorio empresaRepositorio;
    private EmpresaMapper empresaMapper;

    private final UsuarioRepositorio usuarioRepositorio;
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

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public List<Usuario> listarUsuarios() {
        validarTenant();
        Long empresaId = TenantContext.getTenantId();
        return usuarioRepositorio.findByEmpresaId(empresaId);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public Usuario buscarPorId(Long id) {
        validarTenant();
        Long empresaId = TenantContext.getTenantId();
        Usuario usuario = usuarioRepositorio.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));
        return usuario;
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void guardarUsuario(UsuarioDTORequest usuario) {
        validarTenant();
        Long empresaId = TenantContext.getTenantId();

        if (!usuario.getRol().equals("ADMIN") && !usuario.getRol().equals("CAJERO")) {
            throw new BusinessException("Rol inválido. Solo se permiten ADMIN o CAJERO");
        }

        Empresa empresa = empresaRepositorio.findById(empresaId)
                .orElseThrow(() -> new BusinessException("Empresa no encontrada"));

        String contraseñaEncriptada = passwordEncoder.encode(usuario.getContrasenna());
        usuario.setContrasenna(contraseñaEncriptada);

        Usuario entidad = usuarioMapper.toEntity(usuario);
        entidad.setEmpresa(empresa);
        entidad.setEstaActivo(true);
        usuarioRepositorio.save(entidad);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void actualizarUsuario(Long id, UsuarioDTORequest usuario) {
        validarTenant();
        Long empresaId = TenantContext.getTenantId();
        
        Usuario usuarioExistente = usuarioRepositorio.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));

        usuarioExistente.setNombre(usuario.getNombre());
        usuarioExistente.setNombreUsuario(usuario.getNombreUsuario());
        usuarioExistente.setEmail(usuario.getEmail());
        usuarioExistente.setRol(usuario.getRol());
        usuarioExistente.setEstaActivo(usuario.getEstaActivo());

        if (usuario.getContrasenna() != null && !usuario.getContrasenna().isEmpty()) {
            usuarioExistente.setContrasenna(passwordEncoder.encode(usuario.getContrasenna()));
        }

        usuarioRepositorio.save(usuarioExistente);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void eliminarUsuario(Long id) {
        validarTenant();
        Long empresaId = TenantContext.getTenantId();
        
        Usuario usuario = usuarioRepositorio.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));

        validarUsuarioActivo(usuario);

        usuario.setEstaActivo(false);
        usuarioRepositorio.save(usuario);
    }

    @Transactional
    @PreAuthorize("hasRole('SUPERADMIN')")
    public void crearUsuarioAdmin(Empresa empresa) {
        System.out.println("\n🔍 [crearUsuarioAdmin] ========== INICIO ==========");

        Long tenantId = empresa.getId();
        System.out.println("🏢 [crearUsuarioAdmin] Tenant ID obtenido: " + tenantId);

        if (tenantId == null) {
            System.err.println("❌ [crearUsuarioAdmin] ERROR: Tenant ID es NULL");
            throw new BusinessException("No hay tenant en el contexto");
        }

        System.out.println("🔎 [crearUsuarioAdmin] Buscando empresa con ID: " + tenantId);

        System.out.println("✅ [crearUsuarioAdmin] Empresa encontrada:");
        System.out.println("   - ID: " + empresa.getId());
        System.out.println("   - Subdominio: " + empresa.getSubdominio());
        System.out.println("   - Nombre: " + empresa.getNombreEmpresa());
        System.out.println("   - Email: " + empresa.getEmail());

        System.out.println("👤 [crearUsuarioAdmin] Creando usuario administrador");

        Usuario admin = new Usuario();
        admin.setNombre("admin");
        admin.setNombreUsuario("admin");
        admin.setContrasenna(passwordEncoder.encode("123456"));
        admin.setRol("ADMIN");
        admin.setEmail("admin@" + empresa.getSubdominio() + ".com");
        admin.setEmpresa(empresa);
        admin.setEstaActivo(true);
        admin.setEsSuperadmin(false);

        System.out.println("📋 [crearUsuarioAdmin] Datos del usuario a guardar:");
        System.out.println("   - Nombre: " + admin.getNombre());
        System.out.println("   - Usuario: " + admin.getNombreUsuario());
        System.out.println("   - Rol: " + admin.getRol());
        System.out.println("   - Activo: " + admin.getEstaActivo());
        System.out.println("   - Email: " + admin.getEmail());
        System.out.println("   - empresa: " + (admin.getEmpresa() != null ? admin.getEmpresa().getId() : "null"));

        System.out.println("💾 [crearUsuarioAdmin] Guardando usuario en BD...");
        Usuario usuarioGuardado = usuarioRepositorio.save(admin);

        System.out.println("✅ [crearUsuarioAdmin] Usuario guardado con ID: " + usuarioGuardado.getId());
        System.out.println("🔍 [crearUsuarioAdmin] ========== FIN ==========\n");
    }
}