# AGENTS.md - Developer Guide for MVP Restaurant Project

## Project Overview
- **Type:** Spring Boot 3.5.11 with Java 21
- **Build:** Maven (`./mvnw`)
- **Database:** MySQL with JPA/Hibernate
- **Template:** Thymeleaf
- **Security:** Spring Security with BCrypt
- **Reports:** Apache POI (Excel export)

## Build Commands
```bash
./mvnw clean install          # Build + run tests
./mvnw spring-boot:run        # Run application
./mvnw clean package          # Package (skip tests)
./mvnw compile                # Compile only
./mvnw test                   # Run all tests
./mvnw test -Dtest=ClassName  # Run single test class
./mvnw test -Dtest=ClassName#methodName  # Run specific test method
```

## Code Style

### Naming Conventions
- **Classes:** PascalCase (`ProductoService`, `InventarioController`)
- **Methods/variables:** camelCase (`listarActivos`, `empresaId`)
- **Packages:** lowercase (`services`, `controllers`, `models`)
- **Constants:** UPPER_SNAKE_CASE (`EFECTIVO`, `TRANSFERENCIA`)
- **Database columns:** snake_case via `@Column(name = "nombre_columna")`

### Imports Order (Required)
1. Java standard library (`java.util`, `java.math`)
2. Spring framework (`org.springframework.*`)
3. Third-party libraries (Lombok `@RequiredArgsConstructor`, MapStruct, Jakarta Validation)
4. Project imports (`com.mvprestaurante.mvp.*`)

### Entity Style
- Use Lombok `@Data`, `@Entity`, `@Table`, `@Column`
- Use `Optional` for nullable relationships
- Use `BigDecimal` for monetary values, never `Double`
- Always include `estaActivo` for soft deletes
- Use `LocalDateTime` for timestamps

### Service Layer (Required Pattern)
- **Controllers:** SOLO reciben requests, llaman servicios, pasan model/redirect attributes
- **Services:** TODA la lógica de negocio, validaciones, parsing de parámetros
- Use `@Transactional` on all service methods
- Use `readOnly = true` for read operations
- Validate tenant with `validarTenant()` method

### Error Handling
- Throw `BusinessException` for business logic errors
- Throw `DuplicateResourceException` for duplicate entries
- Use `@ControllerAdvice` (`GlobalExceptionHandler`) for global exception handling
- Return meaningful error messages in Spanish
- Use `BindingResult` for form validation in controllers

### Multi-Tenant Pattern
- Get tenant via `TenantContext.getTenantId()`
- Validate with `validarTenant()` in every service method
- Throw `BusinessException` if no tenant
- Tenant filter resolves subdomain before authentication

### Testing
- Use `@SpringBootTest` for integration tests
- Use `@DataJpaTest` for repository tests
- Use `@MockBean` for service mocking in controller tests
- Test naming: `MethodName_Scenario_ExpectedResult`

### Thymeleaf Templates
- Place in `src/main/resources/templates/`
- Use fragments for common elements (navbar, sidebar, footer)
- Use th:href, th:action for links/forms
- Use th:object with form backing beans

## Project Structure
```
src/main/java/com/mvprestaurante/mvp/
├── controllers/   # @Controller - Thymeleaf endpoints
├── services/      # @Service - business logic
├── repositories/  # JpaRepository interfaces
├── models/        # JPA entities with Lombok
├── DTO/           # Data Transfer Objects
├── mapper/        # MapStruct interfaces
├── exceptions/    # Custom exceptions + @ControllerAdvice
├── config/        # @Configuration classes
├── security/      # CustomUserDetails, CustomUserDetailsService
└── multitenant/   # TenantContext, TenantFilter, TenantResolverService
```

## Business Rules Summary
- **Productos:** Con/sin receta, soft delete, unique name per company
- **Recetas:** 1:1 con producto, calcula stock desde ingredientes
- **Ventas:** Sequential numeroVenta, actualiza stock, soporta cierre X/Z
- **Compras:** Registra compras, actualiza stock, unique invoice per company
- **Inventario:** MovimientoStock registra entradas/salidas
- **Usuarios:** Roles ADMIN, CAJERO, COCINERO, INVENTARIO, BCrypt passwords
- **Superadmin:** Login separado `/superadmin/login`, gestión de empresas, `esSuperadmin = true`
- **Empresas:** Se crean inactivas, esperan activación del superadmin