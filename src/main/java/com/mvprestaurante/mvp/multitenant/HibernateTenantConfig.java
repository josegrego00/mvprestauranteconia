package com.mvprestaurante.mvp.multitenant;

import java.util.Map;

import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.stereotype.Component;

@Component
public class HibernateTenantConfig implements HibernatePropertiesCustomizer {
    private final TenantIdentifierResolver tenantIdentifierResolver;

    public HibernateTenantConfig(TenantIdentifierResolver tenantIdentifierResolver) {
        this.tenantIdentifierResolver = tenantIdentifierResolver;
    }

    @Override
    public void customize(Map<String, Object> hibernateProperties) {
        // ✅ ESTE es el que necesitas (resolvedor)
        hibernateProperties.put(
                "hibernate.tenant_identifier_resolver", // ← viene de MULTI_TENANT_IDENTIFIER_RESOLVER
                tenantIdentifierResolver);

        // ✅ Esto también es correcto - la estrategia la pones manualmente
        hibernateProperties.put(
                "hibernate.multi_tenancy", // ← aunque no está en MultiTenancySettings, Hibernate la reconoce
                "DISCRIMINATOR");
    }

}
