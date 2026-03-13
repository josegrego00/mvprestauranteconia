package com.mvprestaurante.mvp.multitenant;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class TenanFilter extends OncePerRequestFilter {

    private final SubdomainExtractor extractor;
    private final TenantResolverService resolver;

    // Lista de dominios principales que NO requieren tenant
    private final List<String> mainDomains = Arrays.asList(
            "mibombay.com",
            "localhost",
            "127.0.0.1");

    public TenanFilter(SubdomainExtractor extractor,
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

        // ✅ 2. Verificar si es un subdominio de localhost (ej: empanadas.localhost)
        if (host.endsWith(".localhost")) {
            return false; // NO es dominio principal, debe aplicar filtro
        }

        // ✅ 3. Rutas públicas
        return path.equals("/") ||
                path.startsWith("/registro") ||
                path.startsWith("/css") ||
                path.startsWith("/js") ||
                path.startsWith("/images");
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
            response.sendRedirect("http://mibombay.com");
            return;
        }

        Long tenantId = resolver.resolveTenantId(subdominio);
        TenantContext.setTenantId(tenantId);

        System.out.println("===== TENANT FILTER =====");
        System.out.println("Host: " + host);
        System.out.println("Subdominio extraído: " + subdominio);
        System.out.println("Tenant ID: " + tenantId);
        System.out.println("=========================");
        try {

            filterChain.doFilter(request, response);

        } finally {

        }
    }
}