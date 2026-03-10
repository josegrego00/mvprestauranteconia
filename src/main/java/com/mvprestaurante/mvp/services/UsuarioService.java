package com.mvprestaurante.mvp.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.mvprestaurante.mvp.DTO.EmpresaDTOResponse;
import com.mvprestaurante.mvp.DTO.UsuarioDTORequest;
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

    public void guardarUsuario(UsuarioDTORequest usuario) {
        System.out.println("🔍 [guardarUsuario] INICIO - Guardando usuario: " + usuario.getNombreUsuario());

        // Encriptar la contraseña antes de guardar
        String contraseñaEncriptada = passwordEncoder.encode(usuario.getContrasenna());
        usuario.setContrasenna(contraseñaEncriptada);
        System.out.println("🔐 [guardarUsuario] Contraseña encriptada correctamente");

        if (usuario.getRol().equalsIgnoreCase("administrador")) {
            usuario.setRol("ADMIN");
            System.out.println("👑 [guardarUsuario] Rol asignado: ADMIN");
        } else {
            usuario.setRol("CAJERO");
            System.out.println("🧑‍💼 [guardarUsuario] Rol asignado: CAJERO");
        }

        Usuario entidad = usuarioMapper.toEntity(usuario);
        System.out.println("🔄 [guardarUsuario] Mapper convertido a entidad");

        usuarioRepositorio.save(entidad);
        System.out.println("✅ [guardarUsuario] Usuario guardado exitosamente en BD");
    }

    @Transactional
    public void crearUsuarioAdmin(Empresa empresa) {
        System.out.println("\n🔍 [crearUsuarioAdmin] ========== INICIO ==========");

        // 1. Obtener tenantId del contexto
        Long tenantId = empresa.getId();
        System.out.println("🏢 [crearUsuarioAdmin] Tenant ID obtenido: " + tenantId);

        if (tenantId == null) {
            System.err.println("❌ [crearUsuarioAdmin] ERROR: Tenant ID es NULL");
            throw new RuntimeException("No hay tenant en el contexto");
        }

        // 2. Buscar empresa por ID
        System.out.println("🔎 [crearUsuarioAdmin] Buscando empresa con ID: " + tenantId);

        System.out.println("✅ [crearUsuarioAdmin] Empresa encontrada:");
        System.out.println("   - ID: " + empresa.getId());
        System.out.println("   - Subdominio: " + empresa.getSubdominio());
        System.out.println("   - Nombre: " + empresa.getNombreEmpresa());
        System.out.println("   - Email: " + empresa.getEmail());

        // 3. Crear usuario admin
        System.out.println("👤 [crearUsuarioAdmin] Creando usuario administrador");

        Usuario admin = new Usuario();
        admin.setNombre("admin");
        admin.setNombreUsuario("admin");
        admin.setContrasenna(passwordEncoder.encode("123456"));
        admin.setRol("ADMIN");
        admin.setEmail("cliente@gail.com");
        admin.setEmpresa(empresa);
        admin.setEstaActivo(true);

        System.out.println("📋 [crearUsuarioAdmin] Datos del usuario a guardar:");
        System.out.println("   - Nombre: " + admin.getNombre());
        System.out.println("   - Usuario: " + admin.getNombreUsuario());
        System.out.println("   - Rol: " + admin.getRol());
        System.out.println("   - Activo: " + admin.getEstaActivo());
        System.out.println("   - Email: " + admin.getEmail());
        System.out.println("   - empresa: " + (admin.getEmpresa() != null ? admin.getEmpresa().getId() : "null"));

        // 4. Guardar en BD
        System.out.println("💾 [crearUsuarioAdmin] Guardando usuario en BD...");
        Usuario usuarioGuardado = usuarioRepositorio.save(admin);

        System.out.println("✅ [crearUsuarioAdmin] Usuario guardado con ID: " + usuarioGuardado.getId());
        System.out.println("🔍 [crearUsuarioAdmin] ========== FIN ==========\n");
    }
}