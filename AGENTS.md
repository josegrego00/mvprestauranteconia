# AGENTS.md - MVP Restaurant API

## Tech Stack
- Spring Boot 3.5.11 + Java 21 + Maven
- MySQL + JPA/Hibernate
- **API REST** → Backend listo para conectar con React
- Spring Security (BCrypt) + Apache POI (Excel)
- SpringDoc OpenAPI (Swagger)

## Build Commands
```bash
./mvnw clean install     # Full build
./mvnw spring-boot:run   # Run dev
./mvnw test             # Run all tests
./mvnw test -Dtest=ClassName#methodName  # Single test
```

## Architecture
- **API REST**: `/api/v1/*` - Endpoints JSON para React
- **Multi-tenant**: Subdominio como tenant (dev: localhost, prod: mibombay.com)
- **Superadmin**: Separate login at `/api/v1/auth/superadmin/login`
- **Demo company**: "restamodelo" with sample data
- **Package structure**: `controllers/`, `services/`, `repositories/`, `models/`, `DTO/`, `mapper/`, `exceptions/`, `config/`, `security/`, `multitenant/`, `utils/`

## API Base URL
```
http://localhost:8080/api/v1
```

## API Endpoints (Empresa)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/empresas` | List all empresas |
| GET | `/api/v1/empresas/{id}` | Get empresa by ID |
| GET | `/api/v1/empresas/subdominio/{subdominio}` | Get by subdominio |
| POST | `/api/v1/empresas` | Create empresa |
| PUT | `/api/v1/empresas/{id}` | Update empresa |
| DELETE | `/api/v1/empresas/{id}` | Delete (deactivate) empresa |
| POST | `/api/v1/empresas/{id}/activar` | Activate empresa |
| POST | `/api/v1/empresas/{id}/desactivar` | Deactivate empresa |

## API Endpoints (Usuario)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/usuarios` | List usuarios by empresa |
| GET | `/api/v1/usuarios/{id}` | Get usuario by ID |
| POST | `/api/v1/usuarios` | Create usuario |
| PUT | `/api/v1/usuarios/{id}` | Update usuario |
| DELETE | `/api/v1/usuarios/{id}` | Deactivate usuario |
| POST | `/api/v1/usuarios/{id}/activar` | Activate usuario |

## Swagger/OpenAPI
- UI: `/swagger-ui.html`
- Docs: `/api-docs`

## MapStruct Gotcha
When adding new DTOs/mappers, ensure pom.xml annotation processors run in correct order (lombok → mapstruct-processor). See pom.xml:106-121 for config template.

## Business Rules
- Productos: con receta (no stock directo, calculated) vs sin receta (stock managed)
- Recetas: 1:1 with products, stock = min(stock_ingredient / cantidad)
- Ventas: blocks products with zero stock; requires open day (no cierre Z)
- Ingredientes: name unique per empresa, protected deletion if in recipes
- Compras: updates stock automatically; validates unique invoice number

## Database
Edit `src/main/resources/application.properties` or set env vars: `DB_URL`, `DB_USER`, `DB_PASSWORD`.

## Testing
- Mockito + JUnit 5 tests en `src/test/java/`
- DataProviders estáticos en `src/test/java/.../testdata/`
- Tests para EmpresaService, EmpresaController, UsuarioService, UsuarioController

## Audit Logging
- Logs guardados en `./logs/audit/audit.log`
- Formato: `[AUDIT] timestamp | Usuario: name | Acción: ACTION | Entidad: Entity | Detalle | Resultado: SUCCESS/FAILURE`

## Security (UserDetails)
- **UserDetailsServiceImpl**: Servicio unificado para autenticación
- `loadUserForNormalLogin(username, empresaId)`: Para login de empresas (filtro tenant)
- `loadUserForSuperAdmin(username)`: Para login superadmin (global)