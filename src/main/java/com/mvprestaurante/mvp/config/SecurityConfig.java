package com.mvprestaurante.mvp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.mvprestaurante.mvp.multitenant.SubdomainExtractor;
import com.mvprestaurante.mvp.multitenant.TenantFilter;  
import com.mvprestaurante.mvp.multitenant.TenantResolverService;
import com.mvprestaurante.mvp.security.UserDetailsServiceImpl;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final SubdomainExtractor extractor;
    private final TenantResolverService resolver;
    private final UserDetailsServiceImpl userDetailsService;

    // ========================================================================
    // CONFIGURACIÓN DE AUTENTICACIÓN
    // ========================================================================
    // Este servicio unificado maneja DOS tipos de autenticación:
    //
    // 1. SUPERADMIN (Order 1): Accede por /superadmin/**
    //    - Usa loadUserForSuperAdmin() - busca globalmente sin filtro empresa
    //    - No requiere contexto de tenant
    //
    // 2. USUARIOS NORMALES (Order 2): Acceden por el resto de endpoints
    //    - Usa loadUserForNormalLogin() - busca con filtro empresa (tenantId)
    //    - Requiere contexto de tenant configurado por TenantFilter
    // ========================================================================

    public SecurityConfig(SubdomainExtractor extractor,
                    TenantResolverService resolver,
                    UserDetailsServiceImpl userDetailsService) {
        this.extractor = extractor;
        this.resolver = resolver;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ========================================================================
    // FILTER CHAIN 1: SUPERADMIN
    // ========================================================================
    // Este filter chain tiene PRIORIDAD (Order 1).
    // Intercepta todas las requests a /superadmin/** y /setup.
    //
    // SEGURIDAD:
    // - Solo permite acceso a usuarios con rol ADMINDEV
    // - No tiene filtro de tenant (busca usuario globalmente)
    // - Form login configurado para /superadmin/login
    // ========================================================================

    @Bean
    @Order(1)
    public SecurityFilterChain superAdminFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/superadmin/**", "/setup")
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/superadmin/login", "/setup").permitAll()
                .requestMatchers("/superadmin/**").hasRole("ADMINDEV"))
            .formLogin(form -> form
                .loginPage("/superadmin/login")
                .loginProcessingUrl("/superadmin/login")
                .defaultSuccessUrl("/superadmin/empresas", true)
                .failureUrl("/superadmin/login?error=true")
                .permitAll())
            .logout(logout -> logout
                .logoutSuccessUrl("/superadmin/login?logout=true")
                .permitAll())
            .userDetailsService(username -> userDetailsService.loadUserForSuperAdmin(username));

        return http.build();
    }

    // ========================================================================
    // FILTER CHAIN 2: USUARIOS NORMALES (EMPRESAS)
    // ========================================================================
    // Este filter chain tiene PRIORIDAD (Order 2).
    // Intercepta todas las demás requests.
    //
    // FLUJO:
    // 1. TenantFilter extrae el subdominio y configura TenantContext
    // 2. Se busca el usuario filtrando por nombre_usuario + empresa_id
    // 3. Se valida que no sea ADMINDEV
    //
    // SEGURIDAD:
    // - Excluye /superadmin/** (bloqueados con denyAll)
    // - Exige autenticación para cualquier otra request
    // - Cada empresa ve solo sus propios datos
    // ========================================================================

    @Bean
    @Order(2)
    public SecurityFilterChain normalFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .addFilterBefore(new TenantFilter(extractor, resolver),
                UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/", "/registro", "/empresa/guardar", "/empresa/espera-activacion", "/css/**",
                    "/js/**", "/error/**", "/salir", "/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
                .permitAll()
                .requestMatchers("/login").permitAll()
                .requestMatchers("/superadmin/**").denyAll()
                .anyRequest().authenticated())
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard", true)
                .permitAll())
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout=true")
                .permitAll())
            .securityContext(context -> context
                .requireExplicitSave(false))
            .userDetailsService(username -> {
                // ====================================================================
                // NOTA IMPORTANTE: TenantFilter debe ejecutarse ANTES de aquí
                // ====================================================================
                // El filtro TenantFilter configura TenantContext con el empresaId
                // del subdominio. Sin eso, este lambda fallará.
                // ====================================================================
                Long empresaId = com.mvprestaurante.mvp.multitenant.TenantContext.getTenantId();
                return userDetailsService.loadUserForNormalLogin(username, empresaId);
            });

        return http.build();
    }
}