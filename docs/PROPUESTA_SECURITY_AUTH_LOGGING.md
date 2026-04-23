# Propuesta: Sistema de Logging de Seguridad

## Propósito
Crear un sistema de logging **independiente** para autentificaciones:
- Login exitoso
- Login fallido (credenciales incorrectas)
- Logout
- Intentos de acceso denegado
- **Esto aplica tanto para ADMIN como ADMINDEV**

---

## Ubicación de logs

| Log | Archivo |
|-----|---------|
| Auditoría general | `./logs/audit/audit.log` |
| **Security Auth** | `./logs/security/security.log` (nuevo) |

---

## Estructura propuesta

### Nueva clase: SecurityAuthLogger.java

```java
@Component
public class SecurityAuthLogger {

    private static final Logger securityLog = LoggerFactory.getLogger("security");

    // Métodos:
    // - logLoginExito(usuario, portal)
    // - logLoginFallo(usuario, portal, razon)
    // - logLogout(usuario, portal)
    // - logAccesoDenegado(usuario, uri, razon)
}
```

### Formato del log

```
[SECURITY] 2026-04-23 14:30:00 | Tipo: LOGIN_SUCCESS | Usuario: joseKLMora | Portal: SUPERADMIN | IP: 127.0.0.1
[SECURITY] 2026-04-23 14:30:00 | Tipo: LOGIN_FAILURE | Usuario: joseKLMora | Portal: SUPERADMIN | Razón: Contraseña incorrecta | IP: 127.0.0.1
[SECURITY] 2026-04-23 14:30:00 | Tipo: LOGOUT | Usuario: admin | Portal: EMPRESA | IP: 127.0.0.1
[SECURITY] 2026-04-23 14:30:00 | Tipo: ACCESS_DENIED | Usuario: anonymous | URI: /admin/config | Razón: No autenticado | IP: 127.0.0.1
```

---

## Integración

### 1. UserDetailsServiceImpl

```java
// En loadUserForNormalLogin() y loadUserForSuperAdmin()
// - logLoginExito() cuando el usuario se carga correctamente
// - logLoginFallo() cuando no se encuentra o contraseña incorrecta
```

### 2. SecurityConfig - Handlers personalizados

| Handler | Dónde |
|---------|-------|
| AuthenticationFailureHandler | SecurityConfig - para logins fallidos |
| LogoutHandler | SecurityConfig - para logout |
| AccessDeniedHandler | SecurityConfig - para acceso denegado |

### 3. Configuración Logback (logback-spring.xml)

```xml
<appender name="SECURITY_FILE">
    <file>${LOG_PATH}/security/security.log</file>
    <encoder>
        <pattern>[SECURITY] %d{yyyy-MM-dd HH:mm:ss} | %msg%n</pattern>
    </encoder>
</appender>

<logger name="security" level="INFO" additivity="false">
    <appender-ref ref="SECURITY_FILE"/>
</logger>
```

---

## Tipos de eventos a registrar

| Tipo | Descripción |
|------|-------------|
| LOGIN_SUCCESS | Login exitoso |
| LOGIN_FAILURE | Credenciales incorrectas |
| LOGOUT | Cerrar sesión |
| ACCESS_DENIED | Acceso denegado a recurso |
| PASSWORD_CHANGED | Cambio de contraseña |

---

## Implementación sugerida

1. **Crear SecurityAuthLogger.java** en `utils/`
2. **Agregar handlers en SecurityConfig.java**
3. **Integrar en UserDetailsServiceImpl**
4. **Configurar logback-spring.xml**
5. **Actualizar AGENTS.md**

---

## Notas técnicas

- Usar `SecurityContextHolder` para obtener el usuario actual
- Obtener IP del request desde `HttpServletRequest`
- Distinguir entre portal (EMPRESA vs SUPERADMIN)
- Log async si hay mucho tráfico