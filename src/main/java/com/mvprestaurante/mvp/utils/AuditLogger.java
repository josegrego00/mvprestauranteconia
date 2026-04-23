package com.mvprestaurante.mvp.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class AuditLogger {

    private static final Logger auditLog = LoggerFactory.getLogger("audit");
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void log(String accion, String entidad, String detalle, boolean success) {
        String usuario = obtenerUsuarioActual();
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String resultado = success ? "SUCCESS" : "FAILURE";

        StringBuilder sb = new StringBuilder();
        sb.append("[AUDIT] ").append(timestamp)
          .append(" | Usuario: ").append(usuario)
          .append(" | Acción: ").append(accion)
          .append(" | Entidad: ").append(entidad)
          .append(" | ").append(detalle)
          .append(" | Resultado: ").append(resultado);

        auditLog.info(sb.toString());
    }

    public void logCrear(String entidad, String identificador) {
        log("CREAR", entidad, "ID: " + identificador, true);
    }

    public void logActualizar(String entidad, String identificador) {
        log("ACTUALIZAR", entidad, "ID: " + identificador, true);
    }

    public void logEliminar(String entidad, String identificador) {
        log("ELIMINAR", entidad, "ID: " + identificador, true);
    }

    public void logListar(String entidad, int cantidad) {
        log("LISTAR", entidad, "Cantidad: " + cantidad, true);
    }

    public void logBuscar(String entidad, String identificador) {
        log("BUSCAR", entidad, "ID: " + identificador, true);
    }

    public void logError(String accion, String entidad, String detalle) {
        log(accion, entidad, detalle, false);
    }

    public void logActivar(String entidad, String identificador) {
        log("ACTIVAR", entidad, "ID: " + identificador, true);
    }

    public void logDesactivar(String entidad, String identificador) {
        log("DESACTIVAR", entidad, "ID: " + identificador, true);
    }

    private String obtenerUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return auth.getName();
        }
        return "ANÓNIMO";
    }
}