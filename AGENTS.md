# AGENTS.md - Developer Guide for MVP Restaurant Project

## Project Overview
- **Type:** Spring Boot 3.5.11 with Java 21
- **Build:** Maven (./mvnw)
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
./mvnw test -Dtest=ClassName#methodName  # Run specific method
```

## Project Structure
```
src/main/java/com/mvprestaurante/mvp/
├── controllers/   # @Controller, @RequestMapping (solo delegar a services)
├── services/      # @Service, toda la lógica de negocio
├── repositories/  # JpaRepository interfaces
├── models/        # JPA entities con Lombok
├── DTO/           # Data Transfer Objects con validaciones
├── mapper/        # MapStruct interfaces
├── exceptions/    # Custom exceptions + @ControllerAdvice
├── config/        # @Configuration classes
├── security/      # Custom UserDetailsService
└── multitenant/   # TenantContext, filters
```

## Code Style

### Naming
- Classes: PascalCase (`ProductoService`)
- Methods/variables: camelCase (`listarActivos`, `empresaId`)
- Packages: lowercase (`com.mvprestaurante.mvp.services`)
- Constants: UPPER_SNAKE_CASE
- DB columns: snake_case via `@Column`

### Imports Order
1. Java stdlib
2. Spring Framework
3. Third-party (Lombok, MapStruct, Jakarta Validation)
4. Project imports

### Entities (Lombok)
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

### DTOs with Validation
```java
@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class EntityDTO {
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotNull(message = "El precio es obligatorio")
    @PositiveOrZero(message = "El precio no puede ser negativo")
    private Double precio;
}
```

**Nota sobre nomenclatura DTOs:**
- NombreentidadDTO: Para el objeto completo (ej: VentaDTO, CompraDTO)
- DetalleentidadDTO: Para detalles/items (ej: DetalleVentaDTO, DetalleCompraDTO)
- SimpleentidadDTO: Para listas (opcional, usar solo si se necesita versión reducida)
- Para casos específicos agregar sufijo descriptivo (ej: ProductoVentaDTO, VentaDetalleDTO)

### Mappers (MapStruct)
```java
@Mapper(componentModel = "spring")
public interface EntityMapper {
    Entity toEntity(DTO dto);
    DTO toDTO(Entity entity);
    DTO toSimpleDTO(Entity entity); // Para listas
}
```

### Services - TODA la lógica de negocio aquí
```java
@Service @RequiredArgsConstructor
public class ResourceService {
    private final ResourceRepository repository;
    
    private void validarTenant() {
        Long empresaId = TenantContext.getTenantId();
        if (empresaId == null) {
            throw new BusinessException("No se ha identificado la empresa");
        }
    }
    
    @Transactional(readOnly = true)
    public Page<Resource> buscar(String search, String filtro, Pageable pageable) {
        validarTenant();
        // Aquí se maneja toda la lógica de filtros
        if (search != null && !search.isEmpty()) {
            return repository.buscarPorNombre(search, pageable);
        }
        return repository.findAll(pageable);
    }
    
    @Transactional
    public Resource guardarDesdeFormulario(Resource resource, Map<String, String> params) {
        validarTenant();
        // Aquí se parsean los parámetros del formulario
        // Aquí se validan todas las reglas de negocio
        if (params == null || params.isEmpty()) {
            throw new BusinessException("Debe agregar al menos un item");
        }
        return repository.save(resource);
    }
    
    @Transactional
    public Resource guardar(Resource resource) {
        validarTenant();
        // Validaciones de negocio
        if (resource.getNombre() == null || resource.getNombre().trim().isEmpty()) {
            throw new BusinessException("El nombre es obligatorio");
        }
        return repository.save(resource);
    }
}
```

### Controllers - SOLO delegar a servicios
```java
@Controller @RequestMapping("/resource")
public class ResourceController {
    @Autowired private ResourceService service;
    
    @GetMapping
    public String listar(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String filtro,
            Model model) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("nombre").ascending());
        Page<Resource> resourcesPage = service.buscar(search, filtro, pageable);
        
        model.addAttribute("resources", resourcesPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", resourcesPage.getTotalPages());
        
        return "resource/lista";
    }
    
    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Resource resource,
            @RequestParam Map<String, String> allParams,
            RedirectAttributes ra) {
        try {
            service.guardarDesdeFormulario(resource, allParams);
            ra.addFlashAttribute("success", "Guardado exitosamente");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/resource/nuevo";
        }
        return "redirect:/resource";
    }
}
```

### Repositories
```java
public interface ResourceRepository extends JpaRepository<Entity, Long> {
    Page<Entity> findByCondition(Long empresaId, Pageable pageable);
    boolean existsByCondition(String name, Long empresaId);
    
    @Query("SELECT r FROM Entity r WHERE r.empresa.id = :tenantId AND r.estaActivo = true")
    Page<Entity> findByEstaActivaTrue(@Param("tenantId") Long tenantId, Pageable pageable);
}
```

### Exception Handling
```java
// Custom exceptions in exceptions/ package
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}

public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String resource, String name) {
        super(String.format("Ya existe %s con el nombre: %s", resource, name));
    }
}

// Global handler
@ControllerAdvice
public class GlobalExceptionHandler {
    private String getRedirectUrl(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        if (referer != null && !referisEmpty()) {
            return "redirect:" + referer;
        }
        return "redirect:/";
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public String handleDuplicate(DuplicateResourceException ex, 
                                  RedirectAttributes ra, HttpServletRequest request) {
        ra.addFlashAttribute("error", ex.getMessage());
        return getRedirectUrl(request);
    }

    @ExceptionHandler(BusinessException.class)
    public String handleBusiness(BusinessException ex, 
                                  RedirectAttributes ra, HttpServletRequest request) {
        ra.addFlashAttribute("error", ex.getMessage());
        return getRedirectUrl(request);
    }
}
```

## Key Patterns

### Controller-Service Separation (CRITICAL)
- **Controllers**: SOLO reciben requests, llaman a servicios, passam model/redirect attributes
- **Services**: TODA la lógica de negocio, validaciones, parsing de parámetros, manejo de excepciones de negocio
- NO mettre lógica de negocio en controllers (no validaciones, no parsing de parámetros, no construcción de entidades complejas)

### Multi-Tenant
- Get tenant via `TenantContext.getTenantId()`
- Validate tenant in every service method using `validarTenant()`
- Throw `BusinessException` if no tenant
- Always filter queries by `empresaId`

### Validation
- Use Jakarta Validation in DTOs (`@NotBlank`, `@NotNull`, `@Positive`, `@PositiveOrZero`)
- Use `BusinessException` for business rule validations in services
- Use `DuplicateResourceException` for duplicate resources
- Controllers catch `RuntimeException` and add flash attribute

### Transaction Management
- Use `@Transactional` on all service methods
- Use `readOnly = true` for read operations
- Never catch exceptions inside transactions

### Security
- BCryptPasswordEncoder for passwords
- Public paths in SecurityConfig: `/`, `/registro`, `/login`, `/css/**`, `/js/**`

### Thymeleaf
- Templates in `src/main/resources/templates/`
- Use fragments in `templates/fragments/`
- Flash attributes: `success`, `error`

## Business Rules

### Productos
1. **Con receta**: No manejan stock directo, se calcula automáticamente desde ingredientes
2. **Sin receta**: Manejan stock directamente, no pueden tener receta
3. **Tipo inmutable**: No se puede cambiar después de creado (con/sin receta)
4. **Receta única**: Una receta solo puede asociarse a un producto
5. **Eliminación**: Solo eliminación lógica (`estaActivo = false`)
6. **Validaciones**:
   - Nombre obligatorio y único por empresa
   - Precio de venta >= 0

### Recetas
1. **Asociación**: Una receta pertenece a un solo producto (relación 1:1)
2. **Ingredientes**: Mínimo uno, sin duplicados, cantidad > 0
3. **Costo**: Se calcula automáticamente (`ingrediente.precioCompra * cantidad`)
4. **Eliminación**: Solo lógica, protegida si está asociada a producto
5. **Stock disponible**: `min(stock_ingrediente / cantidad_necesaria)`
6. **Validaciones**:
   - Nombre obligatorio y único por empresa
   - Al menos un ingrediente
   - Ingredientes sin duplicar
   - Cantidades mayores a 0

### Ingredientes
1. **Nombre único**: No puede repetirse dentro de la empresa
2. **Unidad de medida**: Obligatoria
3. **Eliminación**: Solo lógica, protegida si está en alguna receta
4. **Validaciones**:
   - Nombre obligatorio y único por empresa
   - Unidad de medida obligatoria
   - Stock y precio pueden ser 0

### Compras
1. **Items**: Puede incluir ingredientes y/o productos sin receta
2. **Stock**: Se actualiza automáticamente al guardar
3. **Anulación**: Reversa el stock de los items
4. **Validaciones**:
   - Al menos un item
   - Cantidad > 0
   - Precio >= 0

## API Endpoints (JSON)

| Endpoint | Description |
|---------|-------------|
| `GET /recetas/stock/{id}` | Retorna unidades disponibles de una receta |
| `GET /productos/estimado/{id}` | Retorna stock estimado del producto |

## Módulos

### Reportes
- Dashboard principal (`/dashboard`) con resumen de ventas
- Ventas: hoy, semana, mes
- Top 10 productos más vendidos (últimos 30 días)
- Inventario actual con estado (crítico/bajo/ok)

### Inventario Físico
- Formulario para contar stock al final del día
- Lista ingredientes + productos sin receta
- Input de stock físico
- Cálculo de diferencia en tiempo real (unidades y dinero)
- Resumen: total sobrante/faltante

### Reporte de Inventario
- Historial de inventario por fecha
- Filtro por rango de fechas
- Tabla: inventario inicial, consumo, inventario final, diferencia
- Paginación (20 por página)

## Testing
- Tests in `src/test/java/`
- `@SpringBootTest` for integration
- `@DataJpaTest` for repositories
- `@MockBean` for service mocking

## Database
- Config: `application.properties`
- Default: MySQL `localhost:3306/mvprestaurante`
- DDL auto: `update` (dev only)