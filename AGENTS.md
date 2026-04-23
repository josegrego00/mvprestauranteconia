# AGENTS.md - MVP Restaurant

## Tech Stack
- Spring Boot 3.5.11 + Java 21 + Maven
- MySQL + JPA/Hibernate + Thymeleaf
- Spring Security (BCrypt) + Bootstrap 5 + Apache POI (Excel)

## Build Commands
```bash
./mvnw clean install     # Full build
./mvnw spring-boot:run   # Run dev
./mvnw test             # Run all tests
./mvnw test -Dtest=ClassName#methodName  # Single test
```

## Architecture
- **Multi-tenant**: Subdominio como tenant (dev: localhost, prod: mibombay.com)
- **Superadmin**: Separate login at `/superadmin/login`, manages empresas
- **Demo company**: "restamodelo" with sample data
- **Package structure**: `controllers/`, `services/`, `repositories/`, `models/`, `DTO/`, `mapper/`, `exceptions/`, `config/`, `security/`, `multitenant/`

## MapStruct Gotcha
When adding new DTOs/mappers, ensure pom.xml annotation processors run in correct order (lombok → mapstruct-processor). See pom.xml:106-121 for config template.

## Key Routes
| Path | Description |
|------|-------------|
| `/superadmin/login` | Superadmin login |
| `/registro` | New empresa registration |
| `/dashboard` | Main dashboard |
| `/ventas/nueva` | POS (punto de venta) |
| `/ventas/cierre-x` | Partial day report |
| `/ventas/cierre-z` | Close day (blocks further sales) |
| `/inventario` | Physical inventory |
| `/productos` | Product management |
| `/ingredientes` | Ingredient catalog |
| `/recetas` | Recipe management |
| `/compras` | Purchase management |

## Business Rules (must verify before changes)
- Productos: con receta (no stock directo, calculated) vs sin receta (stock managed)
- Recetas: 1:1 with products, stock = min(stock_ingredient / cantidad)
- Ventas: blocks products with zero stock; requires open day (no cierre Z)
- Ingredientes: name unique per empresa, protected deletion if in recipes
- Compras: updates stock automatically; validates unique invoice number

## Database
Edit `src/main/resources/application.properties` or set env vars: `DB_URL`, `DB_USER`, `DB_PASSWORD`.

## Testing
- No test framework integrated in pom.xml (spring-boot-starter-test present but no test code)
- Full stack verification requires running app + MySQL