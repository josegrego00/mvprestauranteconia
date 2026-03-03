package com.mvprestaurante.mvp.multitenant;

import java.io.IOException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class TenanFilter extends OncePerRequestFilter {

    // se encarga de interceptar cada request, extraer el subdominio, resolver el
    // tenantId
    private final SubdomainExtractor extractor;

    // esta clase se encarga de resolver el tenantId a partir del subdominio
    private final TenantResolverService resolver;

    public TenanFilter(SubdomainExtractor extractor,
            TenantResolverService resolver) {
        this.extractor = extractor;
        this.resolver = resolver;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();

        // ✅ Rutas que NO necesitan tenant (públicas)
        return path.equals("/") || // Landing page
                path.startsWith("/registro") || // Registro de empresas
                path.startsWith("/css") || // Recursos estáticos
                path.startsWith("/js") || // JavaScript
                path.startsWith("/images"); // (imagnes, etc)
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        try {

            String host = request.getServerName();

            String subdominio = extractor.extract(host);

            Long tenantId = resolver.resolveTenantId(subdominio);

            TenantContext.setTenantId(tenantId);

            filterChain.doFilter(request, response);

        } finally {
            TenantContext.clear();
        }
    }

}