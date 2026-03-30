# AGENTS.md - Developer Guide for MVP Restaurant Project

## Project Overview
- **Type:** Spring Boot 3.5.11 with Java 21
- **Build:** Maven (`./mvnw`)
- **Database:** MySQL with JPA/Hibernate
- **Template:** Thymeleaf
- **Security:** Spring Security with BCrypt
- **Reports:** Apache POI (Excel export)

## Commands
```bash
./mvnw clean install          # Build + tests
./mvnw spring-boot:run        # Run app
./mvnw clean package          # Package (skip tests)
./mvnw compile                # Compile only
./mvnw test                   # Run all tests
./mvnw test -Dtest=ClassName  # Run single test class
./mvnw test -Dtest=ClassName#methodName  # Run specific test method
```

## Project Structure
```
src/main/java/com/mvprestaurante/mvp/
├── controllers/   # @Controller - Thymeleaf controllers
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

### Templates (src/main/resources/templates/)
```
templates/
├── login.html
├── registro.html
├── index.html
├── inicio.html (dashboard)
├── error/              # Error pages
├── productos/          # lista, formulario, ver
├── ingredientes/       # lista, formulario, ver
├── recetas/           # lista, formulario, ver, ingredientes
├── ventas/            # lista, nueva, ver, cierre-x, cierre-z
├── compras/            # lista, formulario, ver
├── inventario/         # lista, reporte
├── ajuste-precios/     # lista
├── usuario/            # registro
└── fragments/          # head, navbar, sidebar, footer, scripts
```

## Entities (Models)

| Entity | Description | Relationships |
|--------|-------------|---------------|
| **Empresa** | Tenant (subdomain, name) | OneToMany: Usuario, Ingrediente, Receta, Producto, Venta, Compra |
| **Usuario** | Users with role (ADMIN, CAJERO, COCINERO, INVENTARIO) | ManyToOne: Empresa |
| **Producto** | Menu items (name, price, stock, tieneReceta) | ManyToOne: Empresa, OneToOne: Receta |
| **Ingrediente** | Raw materials (name, stock, unidadMedida) | ManyToOne: Empresa, OneToMany: DetalleReceta |
| **Receta** | Product recipes (precioBruto, precioVenta) | ManyToOne: Empresa, OneToOne: Producto, OneToMany: DetalleReceta |
| **DetalleReceta** | Recipe ingredients (cantidad) | ManyToOne: Receta, Ingrediente |
| **Venta** | Sales (numeroVenta, fecha, method payment) | ManyToOne: Empresa, Cliente, Usuario, OneToMany: DetalleVenta |
| **DetalleVenta** | Products sold in a sale | ManyToOne: Venta, Producto |
| **Compra** | Purchases (numeroFactura, proveedor) | ManyToOne: Empresa, Usuario, OneToMany: DetalleCompra |
| **DetalleCompra** | Items purchased | ManyToOne: Compra, Ingrediente, Producto |
| **Cliente** | Customers | ManyToOne: Empresa |
| **MovimientoStock** | Inventory movements | ManyToOne: Empresa, Ingrediente |
| **InventarioRegistro** | Inventory snapshots | ManyToOne: Empresa, OneToMany: InventarioDetalle |
| **InventarioDetalle** | Inventory items | ManyToOne: InventarioRegistro, Ingrediente, Producto |
| **CierreDia** | Z/X closures | ManyToOne: Empresa, Usuario |

## Services
- **ProductoService** - CRUD, estimated stock, search
- **IngredienteService** - CRUD ingredients
- **RecetaService** - CRUD recipes, ingredient management
- **DetalleRecetaService** - Recipe details
- **VentaService** - Sales, stock validation, Z/X closures
- **CompraService** - Inventory purchases
- **InventarioService** - Physical inventory, reports
- **MovimientoStockService** - Stock movements
- **UsuarioService** - User management
- **EmpresaService** - Company management
- **ClienteService** - Customer management
- **ReporteService** - Dashboard reports

## Controllers
- **IndexController** - Root "/"
- **ProductoController** - "/productos"
- **IngredienteController** - "/ingredientes"
- **RecetaController** - "/recetas"
- **VentaController** - "/ventas"
- **CompraController** - "/compras"
- **InventarioController** - "/inventario"
- **UsuarioController** - "/usuario"
- **EmpresaController** - "/empresa"
- **AjustePreciosController** - "/ajuste-precios"
- **ErrorController** - Error handling

## Code Style

### Naming
- Classes: PascalCase (`ProductoService`)
- Methods/variables: camelCase (`listarActivos`, `empresaId`)
- Packages: lowercase
- Constants: UPPER_SNAKE_CASE
- DB columns: snake_case via `@Column`

### Imports Order
1. Java stdlib → 2. Spring → 3. Third-party (Lombok, MapStruct, Jakarta Validation) → 4. Project

### Testing
- Run single test: `./mvnw test -Dtest=ClassName#methodName`
- Use `@SpringBootTest` for integration tests
- Use `@DataJpaTest` for repository tests
- Use `@MockBean` for service mocking

## Key Patterns

### Controller-Service Separation (CRITICAL)
- Controllers: SOLO reciben requests, llaman a servicios, pasan model/redirect attributes
- Services: TODA la lógica de negocio, validaciones, parsing de parámetros

### Multi-Tenant
- Get tenant via `TenantContext.getTenantId()`
- Validate with `validarTenant()` in every service method
- Throw `BusinessException` if no tenant
- Domains: "mibombay.com" (production), "localhost" (development)
- Subdomain resolution via `TenantFilter` before authentication

### Transaction Management
- Use `@Transactional` on all service methods
- Use `readOnly = true` for read operations

### Soft Deletes
- Entities use `estaActivo = false` for deletion
- Protected entities check before delete (e.g., ingredients in recipes)

## Business Rules

### Productos
- **Con receta**: No stock directly, calculated from ingredients (min(stock_ingrediente / cantidad_necesaria))
- **Sin receta**: Direct stock management
- Soft delete (`estaActivo = false`)
- Unique name per company
- Type is immutable after creation

### Recetas
- 1:1 relationship with producto
- Minimum one ingredient, no duplicates, quantity > 0
- Available stock: `min(stock_ingrediente / cantidad_necesaria)`
- Auto-calculate precioBruto from ingredients
- Protected delete if associated to producto

### Ingredientes
- Unique name per company
- Required unidadMedida
- Protected delete if used in any recipe
- Stock movements recorded

### Ventas
- Sequential numeroVenta per company
- Payment methods: EFECTIVO, TRANSFERENCIA, TARJETA, MIXTO
- Updates stock of productos (sin receta) and ingredients (recetas)
- Generates DetalleVenta for each product
- Supports cierre X (partial) and cierre Z (daily)
- Blocks sales after cierre Z
- Void reverses stock

### Compras
- Register purchases of ingredients and products
- Auto-updates stock
- Generates DetalleCompra
- Unique invoice number per company
- Void reverses stock

### Inventario
- MovimientoStock records entries and exits
- InventarioRegistro for periodic snapshots
- Physical inventory with Excel template download
- Report shows flow per period (initial, consumption, final, difference)

### Clientes
- Name, phone, email (optional)
- Default "Consumidor Final" for quick sales
- Reused if already exists

### Usuarios
- Roles: ADMIN, CAJERO, COCINERO, INVENTARIO
- BCrypt encrypted password
- Soft delete (`estaActivo = false`)

### Multi-Tenant (Subdomain)
- Production domain: mibombay.com
- Development domain: localhost / 127.0.0.1
- Each company has unique subdomain
- TenantFilter extracts subdomain before authentication
- TenantResolverService resolves company from subdomain

## Database
- Config: `application.properties`
- MySQL `localhost:3306/mvprestaurante`
- DDL auto: `update` (dev only)

## Dependencies (pom.xml)
- Spring Boot 3.5.11
- spring-boot-starter-data-jpa
- spring-boot-starter-validation
- spring-boot-starter-security
- spring-boot-starter-thymeleaf
- spring-boot-starter-web
- thymeleaf-extras-springsecurity6
- mysql-connector-j
- lombok
- mapstruct 1.6.3
- poi-ooxml 5.2.5 (Excel reports)
