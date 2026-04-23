package com.mvprestaurante.mvp.multitenant;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class TenantFilter extends OncePerRequestFilter {

    private final SubdomainExtractor extractor;
    private final TenantResolverService resolver;

    // Lista de dominios principales que NO requieren tenant
    private final List<String> mainDomains = Arrays.asList(
            "mibombay.com",
            "localhost",
            "127.0.0.1");

    // Subdominio especial para superadmin
    private final String superAdminSubdomain = "bombaydev";

    public TenantFilter(SubdomainExtractor extractor,
            TenantResolverService resolver) {
        this.extractor = extractor;
        this.resolver = resolver;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        String path = request.getServletPath();
        String host = request.getServerName();

        // ✅ 1. Verificar si es dominio principal (EXACTO, no subdominios)
        boolean isMainDomain = mainDomains.stream()
                .anyMatch(domain -> host.equals(domain)); // SOLO igualdad exacta

        if (isMainDomain) {
            System.out.println("Dominio principal detectado: " + host + " - Sin filtro tenant");
            return true;
        }

        // ✅ 2. Verificar si es el subdominio de superadmin (bombaydev)
        String subdominio = extractor.extract(host);
        if (superAdminSubdomain.equals(subdominio)) {
            System.out.println("SuperAdmin detectado: " + host + " - Sin filtro tenant");
            return true;
        }

        // ✅ 3. Verificar si es un subdominio de localhost (ej: empanadas.localhost)
        if (host.endsWith(".localhost")) {
            return false; // NO es dominio principal, debe aplicar filtro
        }

        // ✅ 4. Rutas públicas
        return path.equals("/") ||
                path.startsWith("/registro") ||
                path.startsWith("/empresa/") ||
                path.startsWith("/css") ||
                path.startsWith("/js") ||
                path.startsWith("/images") ||
                path.startsWith("/error");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String host = request.getServerName();

        // Dominios principales exactos
        if (mainDomains.stream().anyMatch(domain -> host.equals(domain))) {
            filterChain.doFilter(request, response);
            return;
        }

        String subdominio = extractor.extract(host);

        if (subdominio == null || subdominio.isEmpty()) {
            response.sendRedirect("http://localhost:8080/registro");
            return;
        }

        Long tenantId;
        try {
            tenantId = resolver.resolveTenantId(subdominio);
            TenantContext.setTenantId(tenantId);
            
            System.out.println("===== TENANT FILTER =====");
            System.out.println("Host: " + host);
            System.out.println("Subdominio extraído: " + subdominio);
            System.out.println("Tenant ID: " + tenantId);
            System.out.println("=========================");
            
            filterChain.doFilter(request, response);
            
        } catch (ResponseStatusException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                response.sendRedirect("http://localhost:8080/error/subdominio-no-encontrado?subdominio=" + subdominio);
                return;
            }
            if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                response.sendRedirect("http://localhost:8080/empresa/espera-activacion?subdominio=" + subdominio);
                return;
            }
            throw e;
        }
    }
}