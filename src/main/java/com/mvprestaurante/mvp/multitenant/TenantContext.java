package com.mvprestaurante.mvp.multitenant;
import org.springframework.stereotype.Component;

@Component
public class TenantContext {
    
    // ThreadLocal = "cada hilo tiene su propio valor"
    private static final ThreadLocal<String> tenantActual = new ThreadLocal<>();
    
    // Guardar el tenant cuando llegue la petición
    public static void setTenantId(String id) {
        tenantActual.set(id);
    }
    
    // Obtener el tenant actual (para usarlo en consultas)
    public static String getTenantId() {
        return tenantActual.get();
    }
    
    // Limpiar cuando termine la petición
    public static void clear() {
        tenantActual.remove();
    }
}