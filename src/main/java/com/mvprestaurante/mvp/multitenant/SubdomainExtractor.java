package com.mvprestaurante.mvp.multitenant;

import org.springframework.stereotype.Component;

@Component
public class SubdomainExtractor {

    // esta clase se encarga de extraer el subdominio del host

    public String extract(String host) {

        if (host == null || !host.contains("localhost")) {
            return null;
        }

        String[] partes = host.split("\\.");

        if (partes.length < 2) {
            return null;
        }

        String subdominio = partes[0].toLowerCase();

        String regex = "^[a-z0-9-]{3,30}$";

        if (!subdominio.matches(regex)) {
            return null;
        }

        return subdominio;
    }
}