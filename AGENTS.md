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
1. Java standard library (`java.util`, `java.math`, `java.util.Optional`)
2. Spring framework (`org.springframework.*`)
3. Third-party libraries (Lombok `@RequiredArgsConstructor`, MapStruct, Jakarta Validation)
4. Project imports (`com.mvprestaurante.mvp.*`)

### Entity Style
- Use Lombok `@Data`, `@Entity`, `@Table`, `@Column`
- Use `Optional` for nullable relationships
- Use `BigDecimal` for monetary values, never `Double`
- Always include `estaActivo` for soft deletes
- Use `LocalDateTime` for timestamps

### DTO Pattern (Required)
- **Controllers:** Receives and returns DTOs, never entities directly
- **Services:** Convert DTO ↔ Entity using MapStruct mappers
- **Validation:** Use `@Valid` in controller methods with `@ModelAttribute`
- Use `BindingResult` to capture validation errors
- DTOs should have validation annotations (`@NotBlank`, `@PositiveOrZero`, etc.)

### Thymeleaf Forms
- Use `th:object="${object}"` in forms for Spring binding
- Use `th:field="*{field}"` instead of `name` + `th:value`
- This enables automatic data binding and validation

### Service Layer (Required Pattern)
- **Controllers:** SOLO receives requests, call services, pass model/redirect attributes
- **Services:** ALL business logic, validations, parameter parsing
- Use `@Transactional` on all service methods
- Use `readOnly = true` for read operations
- Validate tenant with `validarTenant()` method
- **Repository queries MUST filter by tenantId**, never filter in memory with stream/filter
- **Return DTOs, not entities** - Services must convert entities to DTOs before returning
- **Use @Valid in controllers for DTO validation with annotations**
- **Use BusinessException for all errors** - never DuplicateResourceException

### Repository Filter Pattern (Required)
- **ALL queries MUST include tenantId in WHERE clause**
- Use `@Param("tenantId")` and filter by `empresa.id`
- Examples:
  - `findByIdAndEmpresaId(Long id, Long tenantId)`
  - `findByIdAndEmpresaIdAndEstado(Long id, Long tenantId, String estado)`
- **NEVER use .filter() in service** - filtering is done in repository

### Error Handling
- Throw `BusinessException` for ALL business logic errors
- **NEVER throw DuplicateResourceException** - use BusinessException with message
- Use `@ControllerAdvice` (`GlobalExceptionHandler`) for global exception handling
- Return meaningful error messages in Spanish
- Use `BindingResult` for form validation in controllers
- Use `@Valid` in controller for automatic DTO validation with annotations

### Multi-Tenant Pattern
- Get tenant via `TenantContext.getTenantId()`
- Validate with `validarTenant()` in every service method
- Throw `BusinessException` if no tenant
- Tenant filter resolves subdomain before authentication
- **All repository methods MUST include tenantId in WHERE clause**

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
- Use th:field="*{field}" for form fields

## Project Structure
```
src/main/java/com/mvprestaurante/mvp/
├── controllers/
│   ├── AjustePreciosController.java
│   ├── CompraController.java
│   ├── EmpresaController.java
│   ├── ErrorController.java
│   ├── IndexController.java
│   ├── IngredienteController.java
│   ├── InventarioController.java
│   ├── ProductoController.java
│   ├── RecetaController.java
│   ├── SetupController.java
│   ├── SuperAdminController.java
│   ├── UsuarioController.java
│   └── VentaController.java
├── services/
│   ├── ClienteService.java
│   ├── CompraService.java
│   ├── DetalleRecetaService.java
│   ├── EmpresaService.java
│   ├── IngredienteService.java
│   ├── InventarioService.java
│   ├── MovimientoStockService.java
│   ├── ProductoService.java
│   ├── RecetaService.java
│   - ReporteService.java
│   ├── UsuarioService.java
│   └── VentaService.java
├── repositories/
│   ├── ClienteRepositorio.java
│   ├── CierreDiaRepository.java
│   ├── CompraRepository.java
│   ├── DetalleCompraRepository.java
│   ├── DetalleRecetaRepository.java
│   ├── DetalleVentaRepository.java
│   ├── EmpresaRepositorio.java
│   ├── IngredienteRepository.java
│   ├── InventarioRegistroRepository.java
│   ├── MovimientoStockRepository.java
│   ├── ProductoRepository.java
│   ├── RecetaRepository.java
│   ├── UsuarioRepositorio.java
│   └── VentaRepository.java
├── models/
│   ├── Cliente.java
│   ├── CierreDia.java
│   ├── Compra.java
│   ├── DetalleCompra.java
│   ├── DetalleReceta.java
│   ├── DetalleVenta.java
│   ├── Empresa.java
│   ├── Ingrediente.java
│   ├── InventarioDetalle.java
│   ├── InventarioRegistro.java
│   ├── MovimientoStock.java
│   ├── Producto.java
│   ├── Receta.java
│   ├── Usuario.java
│   └── Venta.java
├── DTO/
│   ├── CompraDetalleDTO.java
│   ├── DetalleCompraDTO.java
│   - DetalleVentaDTO.java
│   ├── EmpresaDTO.java
│   ├── IngredienteDTO.java
│   - InventarioDTO.java
│   ├── InventarioItemDTO.java
│   - InventarioReporteDTO.java
│   ├── ProductoDTO.java
│   ├── ProductoVendidoDTO.java
│   - ProductoVentaDTO.java
│   ├── ReporteCierreDTO.java
│   - ReporteDashboardDTO.java
│   ├── UsuarioDTORequest.java
│   └── UsuarioDTOResponse.java
│   └── VentaDTO.java
├── mapper/
│   ├── CompraMapper.java
│   ├── EmpresaMapper.java
│   ├── IngredienteMapper.java
│   ├── ProductoMapper.java
│   ├── ProductoVentaMapper.java
│   ├── UsuarioMapper.java
│   └── VentaMapper.java
├── exceptions/
│   ├── BusinessException.java
│   ├── DuplicateResourceException.java
│   └── GlobalExceptionHandler.java
├── config/
│   ├── SecurityConfig.java
│   ├── SuperAdminInitializer.java
│   └── MvpApplication.java
├── security/
│   ├── CustomUserDetails.java
│   ├── CustomUserDetailsService.java
│   ├── NormalUserDetailsService.java
│   └── SuperAdminUserDetailsService.java
└── multitenant/
    ├── SubdomainExtractor.java
    ├── TenantContext.java
    ├── TenantResolverService.java
    └── TenanFilter.java
```

## Templates
```
src/main/resources/templates/
├── ajuste-precios/
│   └── lista.html
├── compras/
│   ├── formulario.html
│   ├── lista.html
│   └── ver.html
├── empresa/
│   └── espera-activacion.html
├── error/
│   └── subdominio-no-encontrado.html
├── fragmentos/ (fragments)
│   ├── footer.html
│   ├── head.html
│   ├── navbar.html
│   ├── scripts.html
│   └── sidebar.html
├── ingredientes/
│   ├── formulario.html
│   ├── lista.html
│   └── ver.html
├── inventario/
│   ├── lista.html
│   └── reporte.html
├── login.html
├── login-superadmin.html
├── productos/
│   ├── formulario.html
│   ├── lista.html
│   └── ver.html
├── recetas/
│   ├── formulario.html
│   ├── ingredientes.html
│   ├── lista.html
│   └── ver.html
├── registro.html
├── superadmin/
│   └── empresas/
│       ├── formulario.html
│       └── lista.html
├── usuario/
│   ├── formulario.html
│   ├── lista.html
│   ├── registro.html
│   └── ver.html
├── ventas/
│   ├── cierre-x.html
│   ├── cierre-z.html
│   ├── lista.html
│   ├── nueva.html
│   └── ver.html
└── inicio.html
```

## Business Rules Summary
- **Productos:** Con/sin receta, soft delete, unique name per company
- **Recetas:** 1:1 con producto, calcula stock desde ingredientes
- **Ventas:** Sequential numeroVenta, actualiza stock, soporta cierre X/Z
- **Compras:** Registra compras, actualiza stock, unique invoice per company
- **Inventario:** MovimientoStock registra entradas/salidas, reporte de inventario
- **Usuarios:** Roles ADMIN, CAJERO, COCINERO, INVENTARIO, BCrypt passwords
- **Superadmin:** Login separado `/superadmin/login`, gestión de empresas, `esSuperadmin = true`
- **Empresas:** Se crean inactivas, esperan activación del superadmin
- **Clientes:** Registro y gestión de clientes para ventas
- **Ingredientes:** Gestión de materias primas, vinculados a recetas
- **Cierre X/Z:** Reportes diarios de ventas con cierre de caja
- **Ajuste de precios:** Actualización masiva de precios de productos