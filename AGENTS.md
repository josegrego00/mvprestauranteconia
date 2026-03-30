# AGENTS.md - Developer Guide for MVP Restaurant Project

## Project Overview
- **Type:** Spring Boot 3.5.11 with Java 21
- **Build:** Maven (`./mvnw`)
- **Database:** MySQL with JPA/Hibernate
- **Template:** Thymeleaf
- **Security:** Spring Security with BCrypt

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
├── controllers/   # @Controller - SOLO delegar a servicios
├── services/      # @Service - toda la lógica de negocio
├── repositories/  # JpaRepository interfaces
├── models/        # JPA entities con Lombok
├── DTO/           # Data Transfer Objects con validaciones
├── mapper/        # MapStruct interfaces
├── exceptions/    # Custom exceptions + @ControllerAdvice
├── config/        # @Configuration classes (SecurityConfig)
├── security/      # CustomUserDetails, CustomUserDetailsService
└── multitenant/   # TenantContext, TenanFilter, TenantResolverService
```

### Templates (src/main/resources/templates/)
```
templates/
├── login.html
├── registro.html
├── index.html
├── inicio.html (dashboard)
├── productos/     # lista, formulario, ver
├── ingredientes/  # lista, formulario, ver
├── recetas/       # lista, formulario, ver, ingredientes
├── ventas/        # lista, nueva, ver, cierre-x, cierre-z
├── compras/       # lista, formulario, ver
├── inventario/    # lista, reporte
├── ajuste-precios/# lista
├── usuario/       # registro
└── fragments/     # head, navbar, sidebar, footer, scripts
```

## Entities (Models)

| Entity | Description | Relationships |
|--------|-------------|---------------|
| **Empresa** | Tenant (subdomain, name, plan) | OneToMany: Usuario, Ingrediente, Receta, Producto |
| **Usuario** | Users with role (ADMIN, CAJERO, COCINERO, etc.) | ManyToOne: Empresa |
| **Producto** | Menu items (name, price, stock, tieneReceta) | ManyToOne: Empresa, OneToOne: Receta |
| **Ingrediente** | Raw materials (name, stock, unidadMedida) | ManyToOne: Empresa, OneToMany: DetalleReceta |
| **Receta** | Product recipes (name, precioBruto, precioVenta) | ManyToOne: Empresa, OneToOne: Producto, OneToMany: DetalleReceta |
| **DetalleReceta** | Recipe ingredients (cantidad) | ManyToOne: Receta, Ingrediente |
| **Venta** | Sales (numeroVenta, fecha, method payment) | ManyToOne: Empresa, Cliente, Usuario, OneToMany: DetalleVenta |
| **DetalleVenta** | Products sold in a sale | ManyToOne: Venta, Producto |
| **Compra** | Ingredient purchases | ManyToOne: Empresa |
| **DetalleCompra** | Ingredients purchased | ManyToOne: Compra, Ingrediente |
| **Cliente** | Customers | ManyToOne: Empresa |
| **MovimientoStock** | Inventory movements | ManyToOne: Empresa, Ingrediente |
| **InventarioRegistro** | Inventory records | ManyToOne: Empresa |
| **CierreDia** | Z/X closures | ManyToOne: Empresa, Usuario |

## Services
- **ProductoService** - CRUD, estimated stock, search
- **IngredienteService** - CRUD ingredients
- **RecetaService** - CRUD recipes, ingredient management
- **DetalleRecetaService** - Recipe details
- **VentaService** - Sales, Z/X closures
- **CompraService** - Inventory purchases
- **InventarioService** - Inventory and reports
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
- Subdomain resolution via `TenanFilter` before authentication

### Transaction Management
- Use `@Transactional` on all service methods
- Use `readOnly = true` for read operations

### Soft Deletes
- Entities use `estaActivo = false` for deletion
- Protected entities check before delete (e.g., ingredients in recipes)

## Code Templates

### Entity
```java
@Data @AllArgsConstructor @NoArgsConstructor @Builder
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"nombre", "empresa_id"}))
public class EntityName {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;
}
```

### DTO
```java
@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class EntityDTO {
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotNull @PositiveOrZero
    private Double precio;
}
```

### Service
```java
@Service @RequiredArgsConstructor
public class ResourceService {
    private final ResourceRepository repository;
    
    private void validarTenant() {
        Long empresaId = TenantContext.getTenantId();
        if (empresaId == null) throw new BusinessException("No se ha identificado la empresa");
    }
    
    @Transactional(readOnly = true)
    public Page<Resource> buscar(String search, Pageable pageable) {
        validarTenant();
        // lógica de filtros y búsqueda
        return repository.findAll(pageable);
    }
    
    @Transactional
    public Resource guardar(Resource resource) {
        validarTenant();
        // validaciones de negocio
        return repository.save(resource);
    }
}
```

### Controller
```java
@Controller @RequestMapping("/resource")
public class ResourceController {
    @Autowired private ResourceService service;
    
    @GetMapping
    public String listar(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            Model model) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("nombre").ascending());
        model.addAttribute("resources", service.buscar(search, pageable).getContent());
        return "resource/lista";
    }
    
    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Resource resource,
            @RequestParam Map<String, String> allParams,
            RedirectAttributes ra) {
        try {
            service.guardar(resource);
            ra.addFlashAttribute("success", "Guardado exitosamente");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/resource/nuevo";
        }
        return "redirect:/resource";
    }
}
```

### Exception Handling
```java
public class BusinessException extends RuntimeException {
    public BusinessException(String message) { super(message); }
}

public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String resource, String name) {
        super(String.format("Ya existe %s con el nombre: %s", resource, name));
    }
}

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public String handleBusiness(BusinessException ex, 
            RedirectAttributes ra, HttpServletRequest request) {
        ra.addFlashAttribute("error", ex.getMessage());
        String referer = request.getHeader("Referer");
        return referer != null ? "redirect:" + referer : "redirect:/";
    }
}
```

## Business Rules

### Productos
- **Con receta**: Sin stock directo, se calcula desde ingredientes (min(stock_ingrediente / cantidad_necesaria))
- **Sin receta**: Manejan stock directamente
- Eliminación lógica (`estaActivo = false`)
- Nombre único por empresa

### Recetas
- Relación 1:1 con producto (un producto puede tener una receta)
- Mínimo un ingrediente, sin duplicados, cantidad > 0
- Stock disponible: `min(stock_ingrediente / cantidad_necesaria)`
- Cálculo de precio bruto desde ingredientes

### Ingredientes
- Nombre único por empresa
- Unidad de medida configurable
- Eliminación lógica protegida si está en receta
- Movimientos de stock registrados

### Ventas
- Número de venta secuencial por empresa
- Métodos de pago: EFECTIVO, TRANSFERENCIA, TARJETA, MIXTO
- Actualiza stock de productos (sin receta) y ingredients (recetas)
- Genera DetalleVenta por cada producto
- Soporta cierre X (parcial) y cierre Z (diario)

### Compras
- Registro de compras de ingredientes
- Actualiza stock del ingrediente
- Genera DetalleCompra
- Fecha y proveedor registrado

### Inventario
- MovimientoStock registra entradas y salidas
- InventarioRegistro para snapshots periódicos
- Reportes de stock actual vs mínimo

### Clientes
- Nombre, teléfono, email (opcional)
- Historial de ventas asociadas

### Usuarios
- Roles: ADMIN, CAJERO, COCINERO, INVENTARIO
- Contraseña encriptada con BCrypt
- Eliminación lógica (`estaActivo = false`)

### Multi-Tenant (Subdomain)
- Dominio producción: mibombay.com
- Dominio desarrollo: localhost / 127.0.0.1
- Cada empresa tiene subdominio único
- TenanFilter extrae subdominio antes de autenticación
- TenantResolverService resuelve empresa desde subdominio

## Testing
- `@SpringBootTest` for integration
- `@DataJpaTest` for repositories
- `@MockBean` for service mocking

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
