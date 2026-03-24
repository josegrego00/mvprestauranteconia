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
├── controllers/   # @Controller, @RequestMapping
├── services/      # @Service, business logic
├── repositories/  # JpaRepository interfaces
├── models/        # JPA entities with Lombok
├── DTO/           # Data Transfer Objects
├── mapper/        # MapStruct interfaces
├── exceptions/   # Custom exceptions + @ControllerAdvice
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
3. Third-party (Lombok, MapStruct)
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

### DTOs with MapStruct
```java
@Mapper(componentModel = "spring")
public interface EntityMapper {
    Entity toEntity(DTO dto);
    DTO toDTO(Entity entity);
}
```

### Services
```java
@Service @RequiredArgsConstructor
public class ResourceService {
    private final ResourceRepository repository;
    
    @Transactional(readOnly = true)
    public ReturnType method() { ... }
    
    @Transactional
    public void method() { ... }
}
```

### Controllers
```java
@Controller @RequestMapping("/resource")
public class ResourceController {
    @Autowired private ResourceService service;
    
    @GetMapping public String listar(Model model) { ... }
    
    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute Entity e, 
                          BindingResult result, RedirectAttributes ra) { ... }
}
```

### Repositories
```java
public interface ResourceRepository extends JpaRepository<Entity, Long> {
    Page<Entity> findByCondition(Long empresaId, Pageable pageable);
    boolean existsByCondition(String name, Long empresaId);
}
```

### Exception Handling
```java
// Custom exceptions in exceptions/ package
public class DuplicateResourceException extends RuntimeException { ... }

// Global handler
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(DuplicateResourceException.class)
    public String handleDuplicate(DuplicateResourceException ex, 
                                  RedirectAttributes ra) {
        ra.addFlashAttribute("error", ex.getMessage());
        return "redirect:" + request.getHeader("Referer");
    }
}
```

## Key Patterns

### Multi-Tenant
- Get tenant via `TenantContext.getTenantId()`
- Validate tenant in every service method
- Throw `RuntimeException` if no tenant
- Always filter queries by `empresaId`

### Validation
- Use Jakarta Validation (`@Valid`, `@NotBlank`)
- Handle `BindingResult` in controllers
- Display errors in Thymeleaf with `th:errors`

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

## Testing
- Tests in `src/test/java/`
- `@SpringBootTest` for integration
- `@DataJpaTest` for repositories
- `@MockBean` for service mocking

## Database
- Config: `application.properties`
- Default: MySQL `localhost:3306/mvprestaurante`
- DDL auto: `update` (dev only)