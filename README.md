# MVP Restaurante

Sistema de gestión para restaurantes con soporte multi-empresa (multi-tenant).

## Tech Stack

- **Backend:** Spring Boot 3.5.11 + Java 21
- **Build:** Maven
- **Database:** MySQL + JPA/Hibernate
- **Template:** Thymeleaf
- **Security:** Spring Security con BCrypt
- **UI:** Bootstrap 5 + Bootstrap Icons

## Características

### Punto de Venta (Caja)
- Interfaz rápida para cajeros
- Productos visualizados como botones para selección rápida
- **Stock estimado**: Productos con receta muestran disponibilidad desde ingredientes
- **Validación de stock**: Bloquea productos con stock 0
- **Pagos mixtos**: Efectivo, Tarjeta, Transferencia o combinación
- **Botón Pago Exacto**: Autocompleta el monto total
- **Cambio visual**: Rojo si falta pagar, azul si está completo
- **Redirect automático**: Después de venta exitosa, nueva venta lista
- Persistencia del carrito en errores de validación

### Gestión de Compras
- Registro de compras de ingredientes y productos
- Actualización automática de stock al registrar
- Anulación de compras con reversa de stock
- Validación de número de factura único por empresa

### Gestión de Productos
- Productos con y sin receta
- Control de stock (solo productos sin receta)
- Precio de compra y venta con margen automático
- **Stock estimado**: Cálculo dinámico basado en ingredientes (productos con receta)
- Asociación de recetas existentes o creación de nuevas desde el producto

### Gestión de Recetas
- Recetas asociadas a productos (1:1)
- Ingredientes con cantidades necesarias
- Cálculo automático de costo bruto (precio de ingredientes × cantidad)
- **Stock disponible**: Unidades que se pueden producir según inventario de ingredientes
- Eliminación lógica protegida (no se elimina si está asociada a producto)
- Validación de ingredientes únicos y cantidades válidas

### Gestión de Ingredientes
- Catálogo de ingredientes activo
- Control de stock disponible
- Precio de compra por unidad
- Eliminación lógica protegida (no se elimina si está en alguna receta)
- Validaciones: nombre único, unidad de medida obligatoria

### Gestión de Clientes
- Registro de clientes
- Cliente default "Consumidor Final" para ventas rápidas

### Ajuste de Precios
- Filtros por tipo de producto (con/sin receta)
- Actualización inline de precios de venta

### Multi-Tenant
- Aislamiento de datos por empresa
- Cada empresa tiene su propio subdominio

## Estructura del Proyecto

```
src/main/java/com/mvprestaurante/mvp/
├── controllers/    # Controladores REST/Thymeleaf
├── services/       # Lógica de negocio
├── repositories/   # Repositorios JPA
├── models/         # Entidades JPA
├── DTO/            # Objetos de transferencia de datos
├── mapper/         # Mapeadores MapStruct
├── exceptions/     # Excepciones personalizadas (BusinessException, DuplicateResourceException)
├── config/         # Configuración
└── multitenant/    # Contexto y filtros de tenants
```

## Configuración

### Base de datos
Editar `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/mvprestaurante
spring.datasource.username=root
spring.datasource.password=tu_password
```

### Variables de entorno
```bash
export DB_URL=jdbc:mysql://localhost:3306/mvprestaurante
export DB_USER=root
export DB_PASSWORD=tu_password
```

## Ejecución

```bash
# Compilar
./mvnw clean install

# Ejecutar
./mvnw spring-boot:run

# Solo compilar
./mvnw compile

# Tests
./mvnw test
```

## Rutas Principales

| Ruta | Descripción |
|------|-------------|
| `/` | Login |
| `/dashboard` | Dashboard principal |
| `/productos` | Lista de productos |
| `/productos/nuevo` | Nuevo producto |
| `/productos/editar/{id}` | Editar producto |
| `/recetas` | Lista de recetas |
| `/recetas/nueva` | Nueva receta |
| `/ingredientes` | Lista de ingredientes |
| `/compras` | Lista de compras |
| `/compras/nueva` | Nueva compra |
| `/ventas` | Dashboard de ventas |
| `/ventas/nueva` | Punto de venta (caja) |
| `/ventas/ver/{id}` | Ver detalle de venta |
| `/ajuste-precios` | Ajuste de precios |

## API Endpoints (JSON)

| Endpoint | Descripción |
|---------|-------------|
| `GET /recetas/stock/{id}` | Retorna unidades disponibles de una receta |
| `GET /productos/estimado/{id}` | Retorna stock estimado del producto |

## Reglas de Negocio

### Productos
1. **Con receta**: No manejan stock directo, se calcula automáticamente desde ingredientes
2. **Sin receta**: Manejan stock directamente, no pueden tener receta
3. **Tipo inmutable**: No se puede cambiar después de creado
4. **Receta única**: Una receta solo puede asociarse a un producto
5. **Eliminación**: Solo eliminación lógica (desactivación)

### Recetas
1. **Asociación**: Una receta pertenece a un solo producto
2. **Ingredientes**: Mínimo uno, sin duplicados, cantidad > 0
3. **Costo**: Se calcula automáticamente desde ingredientes
4. **Eliminación**: Protegida si está asociada a un producto
5. **Stock**: Se calcula como mínimo(stock_ingrediente / cantidad_necesaria)

### Ingredientes
1. **Nombre único**: No puede repetirse dentro de la empresa
2. **Unidad de medida**: Obligatoria
3. **Eliminación**: Protegida si está siendo usado en alguna receta
4. **Eliminación**: Solo lógica (desactivación)

### Compras
1. **Items**: Puede incluir ingredientes y/o productos sin receta
2. **Stock**: Se actualiza automáticamente al guardar
3. **Anulación**: Reversa el stock de los items
4. **Número único**: Por empresa, validado en backend

### Ventas
1. **Productos**: Todos los activos (con y sin receta)
2. **Stock**: Directo para sin receta, estimado para con receta
3. **Validación**: Bloquea productos con stock 0
4. **Pago**: Efectivo, Tarjeta, Transferencia o Mixto
5. **Anulación**: Reversa stock de productos/ingredientes

## Excepciones Personalizadas
- **BusinessException**: Errores de negocio (validaciones, reglas)
- **DuplicateResourceException**: Recursos duplicados
- **GlobalExceptionHandler**: Manejo centralizado de excepciones con redirección a página anterior

## Licencia

MIT