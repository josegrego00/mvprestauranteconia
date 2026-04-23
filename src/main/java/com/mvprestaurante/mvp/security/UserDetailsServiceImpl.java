package com.mvprestaurante.mvp.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.mvprestaurante.mvp.models.Usuario;
import com.mvprestaurante.mvp.repositories.UsuarioRepositorio;

import lombok.RequiredArgsConstructor;

/**
 * ============================================================================
 * SERVICIO UNIFICADO DE CARGA DE USUARIOS PARA AUTENTICACIÓN
 * ============================================================================
 *
 * Este servicio maneja la autenticación de usuarios en el sistema.
 * El sistema maneja DOS tipos de usuarios:
 *
 * 1. USUARIOS NORMALES (Empresas): Usuarios con rol ADMIN o CAJERO que pertenecen
 *    a una empresa específica. Deben autenticarse dentro del contexto de su tenant.
 *
 * 2. SUPERADMIN: Usuarios con rol ADMINDEV que tienen acceso global al sistema.
 *    Pueden gestionar todas las empresas. NO tienen filtro de tenant.
 *
 * IMPORTANTE: Este servicio implementa UserDetailsService para integrarse con
 * Spring Security. Cada método corresponde a un tipo diferente de autenticación.
 *
 * ============================================================================
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepositorio repositorio;

    // ========================================================================
    // MÉTODO PRINCIPAL (Implementación de UserDetailsService)
    // ========================================================================
    // Este método es requerido por la interfaz UserDetailsService.
    // Se utiliza como fallback cuando se configura userDetailsService genérico.
    // Delegamos al método de usuarios normales para mantener compatibilidad.
    // ========================================================================

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // ====================================================================
        // ATENCIÓN: Este método NO debe usarse directamente.
        // Usar loadUserForNormalLogin() o loadUserForSuperAdmin() según el caso.
        // ====================================================================
        throw new UnsupportedOperationException(
            "Usar loadUserForNormalLogin(usuario, empresaId) o loadUserForSuperAdmin(usuario)"
        );
    }

    // ========================================================================
    // SECCIÓN 1: AUTENTICACIÓN DE USUARIOS NORMALES (EMPRESAS)
    // ========================================================================
    // Este método se usa cuando un usuario de empresa inicia sesión.
    // Busca el usuario por nombre de usuario Y empresa (tenantId).
    // Excluye usuarios con rol ADMINDEV (son solo para superadmin).
    //
    // FLUJO:
    // 1. Se recibe el nombre de usuario y el ID de la empresa (desde TenantContext)
    // 2. Se busca en la base de datos: WHERE nombre_usuario = ? AND empresa_id = ?
    // 3. Se valida que el usuario NO sea ADMINDEV
    // 4. Se retorna CustomUserDetails con los datos del usuario
    //
    // USO: NormalUserDetailsService (login de empresas)
    // VULNERABILIDAD SI FALLA: Un usuario podría ver datos de otra empresa
    // ========================================================================

    /**
     * Carga un usuario para autenticación normal de empresa.
     *
     * @param username  Nombre de usuario (login)
     * @param empresaId ID de la empresa (tenant) - obtenido de TenantContext
     * @return UserDetails con la información del usuario
     * @throws UsernameNotFoundException si el usuario no existe o no pertenece a la empresa
     *
     * EJEMPLO DE USO:
     * -------------
     * Long empresaId = TenantContext.getTenantId();
     * UserDetails user = service.loadUserForNormalLogin("juan", empresaId);
     *
     * CONSULTA SQL GENERADA:
     * ---------------------
     * SELECT * FROM usuario WHERE nombre_usuario = 'juan' AND empresa_id = 1
     */
    public UserDetails loadUserForNormalLogin(String username, Long empresaId) {
        // --------------------------------------------------------------------
        // VALIDACIÓN DE TENANT
        // --------------------------------------------------------------------
        // Verificamos que exista el contexto de empresa. Sin esto, un atacante
        // podría intentar buscar usuarios sin especificar empresa.
        if (empresaId == null) {
            throw new UsernameNotFoundException(
                "No se ha identificado la empresa. Esto no debería ocurrir."
            );
        }

        // --------------------------------------------------------------------
        // BÚSQUEDA EN BASE DE DATOS
        // --------------------------------------------------------------------
        // Buscamos el usuario filtrando POR empresa (tenantId).
        // Esto es CRÍTICO para la seguridad multi-tenant.
        // Un usuario solo puede existir en UNA empresa.
        Usuario usuario = repositorio.findByNombreUsuarioAndEmpresa_Id(username, empresaId)
                .orElseThrow(() -> new UsernameNotFoundException(
                    "Usuario no encontrado: " + username + " en esta empresa"
                ));

        // --------------------------------------------------------------------
        // VALIDACIÓN DE ROL (SEGURIDAD)
        // --------------------------------------------------------------------
        // Los usuarios con rol ADMINDEV (superadmin) NO deben poder iniciar
        // sesión en empresas. Esto es un control de seguridad adicional.
        // Un superadmin debe usar /superadmin/login exclusivamente.
        if ("ADMINDEV".equals(usuario.getRol())) {
            throw new UsernameNotFoundException(
                "Este usuario no tiene acceso a empresas. Use el portal de superadmin."
            );
        }

        // --------------------------------------------------------------------
        // RETORNO DE DATOS
        // --------------------------------------------------------------------
        // Creamos CustomUserDetails que implementa UserDetails de Spring Security.
        // Este objeto contiene: username, password, authorities (roles), estado, etc.
        return new CustomUserDetails(usuario);
    }

    // ========================================================================
    // SECCIÓN 2: AUTENTICACIÓN DE SUPERADMIN (ADMINDEV)
    // ========================================================================
    // Este método se usa cuando el superadmin inicia sesión en /superadmin/**
    // Busca el usuario SOLO por nombre de usuario, SIN filtro de empresa.
    // El superadmin existe una sola vez en todo el sistema.
    //
    // FLUJO:
    // 1. Se recibe solo el nombre de usuario
    // 2. Se busca en la base de datos: WHERE nombre_usuario = ? AND rol = 'ADMINDEV'
    // 3. Se valida que el rol sea exactamente ADMINDEV
    // 4. Se retorna CustomUserDetails con authorities de ADMINDEV
    //
    // USO: SuperAdminUserDetailsService (/superadmin/login)
    // SEGURIDAD: Sin filtro empresa, busca en toda la base de datos
    // ========================================================================

    /**
     * Carga un superadmin para autenticación global.
     *
     * @param username Nombre de usuario del superadmin
     * @return UserDetails con la información del superadmin
     * @throws UsernameNotFoundException si el superadmin no existe o no tiene rol ADMINDEV
     *
     * EJEMPLO DE USO:
     * -------------
     * UserDetails user = service.loadUserForSuperAdmin("admin");
     *
     * CONSULTA SQL GENERADA:
     * ---------------------
     * SELECT * FROM usuario WHERE nombre_usuario = 'admin'
     *
     * NOTA: Esta consulta NO filtra por empresa_id (es NULL o se ignora).
     * El superadmin es único en todo el sistema.
     */
    public UserDetails loadUserForSuperAdmin(String username) {
        // --------------------------------------------------------------------
        // BÚSQUEDA GLOBAL
        // --------------------------------------------------------------------
        // Buscamos el usuario SIN filtro de empresa.
        // El superadmin debe existir una sola vez en la base de datos.
        // Si existen múltiples usuarios con el mismo nombre, esto es un PROBLEMA.
        Usuario usuario = repositorio.findBynombreUsuario(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                    "Superadmin no encontrado: " + username
                ));

        // --------------------------------------------------------------------
        // VALIDACIÓN ESTRICTA DE ROL
        // --------------------------------------------------------------------
        // Verificación CRÍTICA de seguridad.
        // Aseguramos que el usuario tiene rol ADMINDEV.
        // Un usuario normal (ADMIN/CAJERO) NO debe poder autenticarse como superadmin.
        if (usuario.getRol() == null || !"ADMINDEV".equals(usuario.getRol())) {
            throw new UsernameNotFoundException(
                "El usuario '" + username + "' no tiene permisos de superadmin"
            );
        }

        // --------------------------------------------------------------------
        // RETORNO DE DATOS
        // --------------------------------------------------------------------
        // CustomUserDetails para superadmin.
        // El método getAuthorities() en CustomUserDetails retorna ROLE_ADMINDEV.
        return new CustomUserDetails(usuario);
    }
}