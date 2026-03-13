package com.mvprestaurante.mvp.multitenant;

import org.springframework.stereotype.Component;

@Component
public class SubdomainExtractor {

    public String extract(String host) {
        if (host == null)
            return null;

        // Para subdominios de localhost (ej: empanadas.localhost)
        if (host.endsWith(".localhost")) {
            return host.substring(0, host.indexOf(".localhost"));
        }

        // Para producción (ej: empanadas.mibombay.com)
        if (host.contains(".") && !host.equals("localhost") && !host.equals("mibombay.com")) {
            String[] partes = host.split("\\.");
            String subdominio = partes[0].toLowerCase();
            String regex = "^[a-z0-9-]{3,30}$";
            if (subdominio.matches(regex)) {
                return subdominio;
            }
        }

        return null;
    }
}