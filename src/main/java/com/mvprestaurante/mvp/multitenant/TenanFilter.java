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
            "mibombay.com", // Dominio principal
            "localhost", // Para desarrollo
            "127.0.0.1" // Para desarrollo
    );

    public TenanFilter(SubdomainExtractor extractor,
            TenantResolverService resolver) {
        this.extractor = extractor;
        this.resolver = resolver;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        String path = request.getServletPath();
        String host = request.getServerName();

        // ✅ 1. Verificar si es dominio principal
        boolean isMainDomain = mainDomains.stream()
                .anyMatch(domain -> host.equals(domain) || host.endsWith("." + domain));

        if (isMainDomain) {
            System.out.println("Dominio principal detectado: " + host + " - Sin filtro tenant");
            return true; // No aplicar filtro
        }

        // ✅ 2. Rutas públicas
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

        try {
            String host = request.getServerName();

            // Verificar nuevamente (aunque shouldNotFilter ya lo hace, por seguridad)
            boolean isMainDomain = mainDomains.stream()
                    .anyMatch(domain -> host.equals(domain) || host.endsWith("." + domain));

            if (isMainDomain) {
                filterChain.doFilter(request, response);
                return;
            }

            String subdominio = extractor.extract(host);

            if (subdominio == null || subdominio.isEmpty()) {
                // Si no hay subdominio, redirigir al dominio principal
                response.sendRedirect("http://mibombay.com");
                return;
            }

            Long tenantId = resolver.resolveTenantId(subdominio);
            TenantContext.setTenantId(tenantId);

            filterChain.doFilter(request, response);

        } finally {
            TenantContext.clear();
        }
    }
}