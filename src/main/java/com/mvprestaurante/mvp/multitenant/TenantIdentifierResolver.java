package com.mvprestaurante.mvp.multitenant;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;

@Component
public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver<String> {

    // Obtiene el tenant actual del ThreadLocal
    @Override
    public String resolveCurrentTenantIdentifier() {
        Long tenantId = TenantContext.getTenantId();

        // ⚠️ Durante el inicio de la app, NO hay tenant
        if (tenantId == null) {
            // Para operaciones de bootstrap (crear tablas, etc.)
            return "0"; // Valor por defecto que NO existe como tenant real
        }

        return tenantId.toString();
    }

    // true = Hibernate valida que las sesiones existentes
    @Override
    public boolean validateExistingCurrentSessions() {
        return true; // Validar que las sesiones tengan el tenant correcto
    }

}
