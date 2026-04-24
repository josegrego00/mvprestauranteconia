# REGLAS DE CODIFICACIÓN - MVP RESTAURANT BACKEND

## Guía oficial para el desarrollo del backend del SaaS Multi-tenant

---

## 1. ENTIDADES (JPA)

### Reglas

1. **Solo anotaciones JPA** - No usar anotaciones de negocio en entidades. Solo `@Entity`, `@Table`, `@Column`, `@Id`, `@GeneratedValue`.

2. **BigDecimal para dinero** - Todos los campos que representan precios, costos, totales, balances y cualquier valor monetario DEBEN ser `BigDecimal`, nunca `Double` o `Float`.

3. **Enums para valores fijos** - Los campos con valores predefinidos (tipos, estados, unidades) DEBEN ser `Enum`, nunca `String`.

4. **FetchType.LAZY en colecciones** - Todas las relaciones `@OneToMany` y `@ManyToMany` DEBEN usar `fetch = FetchType.LAZY`.

5. **FetchType.LAZY en relaciones** - Las relaciones `@ManyToOne` y `@OneToOne` DEBEN usar `fetch = FetchType.LAZY` explícitamente.

6. **mappedBy en @OneToMany** - El lado `@OneToMany` de una relación bidireccional DEBE usar `mappedBy`, nunca `@JoinColumn`.

7. **orphanRemoval en composición** - Cuando el hijo no puede existir sin el padre (composición), DEBE usarse `orphanRemoval = true`.

8. **unique en @OneToOne** - En relaciones `@OneToOne` con `@JoinColumn`, DEBE usarse `unique = true` para enforce la unicidad a nivel de base de datos.

9. **nullable = false** - Los campos obligatorios DEBEN tener `nullable = false`.

10. **Sin lógica de negocio** - Las entidades NO deben contener métodos de negocio. Solo getters, setters y anotaciones JPA.

---

## 2. DTOs (Data Transfer Objects)

### Reglas

1. **Validaciones obligatorias** - Todo DTO que recibe datos del cliente DEBE usar anotaciones `jakarta.validation.constraints`.

2. **Mensajes personalizados** - Cada anotación de validación DEBE tener `message = "texto claro del error"`.

3. **Request/Response separados** - Para APIs complejas, los DTOs de entrada y salida DEBEN estar separados (`UsuarioRequestDTO` / `UsuarioResponseDTO`).

4. **BigDecimal para dinero** - Los campos monetarios en DTOs DEBEN ser `BigDecimal`.

5. **Enums directos** - Los campos de tipo fijo DEBEN usar el `Enum` directamente, no `String`.

6. **@Schema para documentación** - Todo DTO DEBE usar `@Schema(description = "...", example = "...")` para Swagger.

7. **Sin lógica** - Los DTOs NO deben contener métodos de negocio, solo transportan datos.

8. **Sin relaciones circulares** - Los DTOs NO deben tener referencias bidireccionales (ej: `ProductoDTO` no debe tener `RecetaDTO` y viceversa).

9. **Usar Lombok** - Los DTOs DEBEN usar `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`.

10. **IDs como Long** - Los campos de identificación DEBEN ser `Long`, nunca tipos primitivos.

---

## 3. REPOSITORIES

### Reglas

1. **Extender JpaRepository** - Todo repositorio DEBE extender `JpaRepository<Entidad, Long>`.

2. **Filtro por tenant** - Todos los métodos de consulta DEBEN filtrar por `empresa.id` usando `@Param("tenantId")`.

3. **existsByNombreAndEmpresaId** - Todo repositorio de entidades con nombre único por empresa DEBE tener el método `boolean existsByNombreAndEmpresaId(String nombre, Long empresaId)`.

4. **findByIdAndEmpresaId** - Todo repositorio DEBE tener el método `Optional<Entidad> findByIdAndEmpresaId(Long id, Long empresaId)` para búsqueda segura.

5. **findByEmpresaId** - Todo repositorio DEBE tener `List<Entidad> findByEmpresaId(Long empresaId)`.

6. **Pageable en consultas masivas** - Los métodos que pueden retornar muchos registros DEBEN aceptar `Pageable` y retornar `Page<Entidad>`.

7. **@Query para queries complejas** - Las consultas JPQL complejas DEBEN usar `@Query` con parámetros nombrados.

8. **Orden por defecto** - Las listas sin paginación DEBEN tener orden explícito (ej: `OrderByNombreAsc`).

---

## 4. SERVICES

### Reglas

1. **@Service** - Todo service DEBE tener la anotación `@Service`.

2. **@Transactional en escritura** - Los métodos que crean, actualizan o eliminan datos DEBEN tener `@Transactional`.

3. **readOnly = true en consultas** - Los métodos de solo lectura DEBEN usar `@Transactional(readOnly = true)`.

4. **AuditLogger obligatorio** - Todo service DEBE inyectar `AuditLogger` y registrar todas las operaciones de negocio.

5. **Validación de tenantId** - El primer paso en todo método DEBE validar `TenantContext.getTenantId()` no sea `null`. Si lo es, lanzar `BusinessException`.

6. **Buscar por nombre antes de guardar** - Antes de guardar una nueva entidad, DEBE verificarse que no exista otra con el mismo nombre en la misma empresa usando `existsByNombreAndEmpresaId()`.

7. **Verificar existencia antes de actualizar** - Al actualizar, DEBE buscarse la entidad con `findByIdAndEmpresaId()` y lanzar excepción si no existe.

8. **Verificar cambio de nombre en actualización** - Al actualizar, si el nombre cambió, DEBE verificarse que el nuevo nombre no esté duplicado.

9. **Verificar pertenencia a empresa** - Toda entidad recuperada DEBE validar que su `empresa.id` coincide con el `tenantId` actual.

10. **Logs informativos** - Las acciones importantes DEBEN registrarse con `log.info()` además de la auditoría.

11. **Inyección por constructor** - Usar `@RequiredArgsConstructor` para inyección de dependencias, nunca `@Autowired` en campos.

12. **Método existePorNombreYEmpresa** - Todo service de entidades con nombre único DEBE exponer el método `boolean existePorNombreYEmpresa(String nombre, Long empresaId)`.

---

## 5. CONTROLLERS

### Reglas

1. **@RestController** - Todo controller DEBE usar `@RestController`, nunca `@Controller` para APIs REST.

2. **@RequestMapping con versión** - La ruta base DEBE incluir la versión de la API: `/api/v1/recurso`.

3. **@Valid en @RequestBody** - Todo DTO recibido en POST o PUT DEBE usar `@Valid`.

4. **ResponseEntity siempre** - Todos los endpoints DEBEN retornar `ResponseEntity<T>`.

5. **Códigos HTTP correctos**:
   - GET exitoso → `200 OK`
   - POST exitoso → `201 CREATED`
   - PUT exitoso → `200 OK`
   - DELETE exitoso → `204 NO_CONTENT`
   - Error de validación → `400 BAD_REQUEST`
   - No encontrado → `404 NOT_FOUND`

6. **@Operation para Swagger** - Todo endpoint DEBE tener `@Operation(summary = "...", description = "...")`.

7. **@ApiResponses** - Los endpoints DEBEN documentar al menos `200` y `400/404`.

8. **Sin lógica de negocio** - El controller NO debe contener lógica de negocio, solo llamar al service y mapear respuestas.

9. **Sin try-catch** - El controller NO debe tener bloques `try-catch`. Las excepciones deben ser manejadas por `@ControllerAdvice`.

10. **Sin auditoría directa** - El controller NO debe llamar directamente a `AuditLogger`. La auditoría es responsabilidad del service.

11. **@PathVariable sin @Valid** - Los `@PathVariable` no necesitan `@Valid`, pero DEBEN validarse en el service.

12. **DTOs en lugar de entidades** - El controller NUNCA debe retornar entidades JPA directamente, siempre DTOs.

---

## 6. PAGINACIÓN

### Reglas

1. **Pageable en listados** - Todo endpoint `GET` que retorna una lista DEBE aceptar `Pageable` como parámetro.

2. **@PageableDefault** - Usar `@PageableDefault(size = 20, sort = "nombre", direction = Sort.Direction.ASC)` para valores por defecto.

3. **Retornar Page<T>** - Los endpoints paginados DEBEN retornar `Page<DTO>` no `List<DTO>`.

4. **Slice para scroll infinito** - Si no se necesita el total de elementos, usar `Slice<T>` para mejor rendimiento.

5. **Parámetros de paginación** - El frontend debe poder enviar:
   - `page`: Número de página (empieza en 0)
   - `size`: Elementos por página
   - `sort`: Campo y dirección (ej: `nombre,asc`)

6. **Paginación en Repository** - Los métodos del repository que retornan listas paginadas DEBEN aceptar `Pageable` y retornar `Page<Entidad>`.

7. **Paginación en Service** - Los métodos del service que retornan listados paginados DEBEN aceptar `Pageable` y retornar `Page<DTO>`.

8. **Mapeo de Page** - Usar `page.map(mapper::toDTO)` para convertir `Page<Entidad>` a `Page<DTO>`.

9. **Orden por defecto** - Si no se especifica orden, DEBE tener un orden por defecto (generalmente por `nombre` o `id` descendente).

10. **Índices en base de datos** - Los campos usados frecuentemente en ordenamiento y filtrado DEBEN tener índices en la base de datos.

---

## 7. AUDITORÍA (AuditLogger)

### Reglas

1. **Inyectar siempre** - `AuditLogger` DEBE inyectarse en todo service que modifique datos.

2. **logCrear** - Usar después de guardar un nuevo registro. Incluir la entidad y el ID del registro creado.

3. **logActualizar** - Usar después de actualizar un registro. Incluir entidad y ID.

4. **logEliminar** - Usar después de eliminar o desactivar un registro. Incluir entidad y ID.

5. **logListar** - Usar al retornar listas. Incluir entidad y cantidad de elementos.

6. **logBuscar** - Usar al buscar por ID. Incluir entidad y ID buscado.

7. **logActivar** - Usar al reactivar un registro. Incluir entidad y ID.

8. **logDesactivar** - Usar al desactivar un registro. Incluir entidad y ID.

9. **logError** - Usar en bloques `catch` antes de relanzar la excepción. Incluir acción, entidad y detalle del error.

10. **ID como String** - El ID debe convertirse a `String` usando `.toString()`.

11. **Éxito/Fracaso** - Los métodos `logXxx` asumen éxito (`true`). Usar `logError` para fracasos.

12. **Usuario automático** - No pasar usuario manualmente. `AuditLogger` obtiene automáticamente el usuario autenticado de `SecurityContextHolder`.

---

## 8. MANEJO DE ERRORES

### Reglas

1. **BusinessException** - Para errores de negocio (duplicados, no encontrados, validaciones de negocio).

2. **@ControllerAdvice global** - Un solo manejador global de excepciones para toda la aplicación.

3. **Mensajes claros** - Los mensajes de error DEBEN ser entendibles por el cliente de la API.

4. **Log de errores** - Todo error capturado en `@ControllerAdvice` DEBE registrarse con `log.error()`.

5. **ErrorResponse estandarizado** - Las respuestas de error DEBEN tener formato consistente:
   ```json
   {
     "timestamp": "2024-01-01T10:00:00",
     "status": 400,
     "message": "Descripción del error",
     "path": "/api/v1/recurso"
   }