# MVP Restaurante

Sistema de gestión para restaurantes con soporte multi-empresa (multi-tenant).

## Tech Stack

- **Backend:** Spring Boot 3.5.11 + Java 21
- **Build:** Maven
- **Database:** MySQL + JPA/Hibernate
- **Template:** Thymeleaf
- **Security:** Spring Security con BCrypt
- **UI:** Bootstrap 5 + Bootstrap Icons
- **Reportes:** Apache POI (Excel)
- **Arquitectura:** Multi-tenant por subdominio

## Características

### Punto de Venta (Caja)
- Interfaz rápida para cajeros con botones de productos
- Productos visualizados para selección rápida
- **Stock estimado**: Productos con receta muestran disponibilidad desde ingredientes
- **Validación de stock**: Bloquea productos con stock 0
- **Pagos**: Efectivo, Tarjeta, Transferencia o Mixto (combinación)
- **Botón Pago Exacto**: Autocompleta el monto total
- **Cambio visual**: Rojo si falta pagar, azul si está completo
- **Redirect automático**: Después de venta exitosa, nueva venta lista
- **Carrito persistente**: Persistencia del carrito en errores de validación
- **Bloqueo por cierre**: No permite ventas después de cierre Z del día
- **Anulación de ventas**: Reversa stock de productos/ingredientes

### Gestión de Productos
- Productos con y sin receta
- Control de stock (solo productos sin receta)
- Precio de compra y venta con margen automático
- **Stock estimado**: Cálculo dinámico basado en ingredientes (productos con receta)
- Asociación de recetas existentes o creación de nuevas desde el producto
- Nombre único por empresa
- Eliminación lógica (desactivación)

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
- Validaciones: nombre único por empresa, unidad de medida obligatoria

### Gestión de Compras
- Registro de compras de ingredientes
- Soporte para comprar productos sin receta
- Actualización automática de stock al registrar
- Anulación de compras con reversa de stock
- Validación de número de factura único por empresa

### Gestión de Clientes
- Registro de clientes (nombre, teléfono, email opcional)
- Cliente default "Consumidor Final" para ventas rápidas
- Reutilización de cliente existente

### Cierre X y Cierre Z
- **Cierre X**: Reporte de ventas parcial del día sin cerrar
- **Cierre Z**: Cierre diario que bloquea ventas posteriores
- Resumen por método de pago
- Total de ventas, cantidad de transacciones
- Registro en historial de cierres

### Ajuste de Precios
- Filtros por tipo de producto (con/sin receta)
- Actualización inline de precios de venta
- Vista rápida de todos los productos

### Dashboard
- Ventas del día, semana y mes (monto y cantidad)
- Top 10 productos más vendidos (últimos 30 días)
- Inventario de productos sin receta (stock bajo/crítico)
- Inventario de ingredientes (stock bajo/crítico)
- Estado del inventario: crítico (<5), bajo (<15), ok

### Inventario Físico
- Formulario para contar stock al final del día
- Lista de ingredientes y productos sin receta
- Input de stock físico para cada item
- Cálculo de diferencia en tiempo real (unidades y dinero)
- Resumen: total sobrante y faltante
- **Descarga de plantilla Excel**: Para facilitar conteo

### Reporte de Inventario
- Historial de inventario por fecha
- Filtro por rango de fechas
- Tabla: inventario inicial, consumo, inventario final, diferencia
- Paginación (20 por página)
- Diferencia en unidades y dinero
- **Exportación Excel**: Descarga de plantilla para conteo

### Gestión de Usuarios
- Roles: ADMIN, CAJERO, COCINERO, INVENTARIO
- Contraseña encriptada con BCrypt
- Eliminación lógica

### Multi-Tenant
- Aislamiento de datos por empresa
- Cada empresa tiene su propio subdominio
- Dominio producción: mibombay.com
- Dominio desarrollo: localhost / 127.0.0.1

### Superadmin
- Panel de administración del sistema
- Login separado en `/superadmin/login`
- Gestión de empresas (activar/desactivar)
- Creación de usuarios admin al activar empresas
- Demo company "restamodelo" con datos de ejemplo

### Registro de Empresas
- Registro público en `/registro`
- Empresas se crean inactivas (esperan activación)
- Página de espera `/empresa/espera-activacion`
- Admin usuario se crea solo cuando superadmin activa la empresa

## Estructura del Proyecto

```
src/main/java/com/mvprestaurante/mvp/
├── controllers/       # Controladores Thymeleaf
├── services/         # Lógica de negocio
├── repositories/     # Repositorios JPA
├── models/           # Entidades JPA
├── DTO/              # Objetos de transferencia de datos
├── mapper/           # Mapeadores MapStruct
├── exceptions/       # Excepciones personalizadas
├── config/          # Configuración
├── security/        # Seguridad Spring
└── multitenant/     # Contexto y filtros de tenants
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

# Test específico
./mvnw test -Dtest=ClassName
./mvnw test -Dtest=ClassName#methodName
```

## Rutas Principales

| Ruta | Descripción |
|------|-------------|
| `/` | Login empresas |
| `/superadmin/login` | Login superadmin |
| `/registro` | Registro de empresa (crea empresa inactiva) |
| `/empresa/espera-activacion` | Página de espera para empresas inactivas |
| `/dashboard` | Dashboard principal |
| `/superadmin/empresas` | Panel superadmin - lista empresas |
| `/superadmin/empresas/activar/{id}` | Activar empresa (crea admin) |
| `/superadmin/empresas/desactivar/{id}` | Desactivar empresa |
| `/productos` | Lista de productos |
| `/productos` | Lista de productos |
| `/productos/nuevo` | Nuevo producto |
| `/productos/editar/{id}` | Editar producto |
| `/productos/ver/{id}` | Ver producto |
| `/recetas` | Lista de recetas |
| `/recetas/nueva` | Nueva receta |
| `/recetas/ver/{id}` | Ver receta |
| `/recetas/ingredientes/{id}` | Gestionar ingredientes de receta |
| `/ingredientes` | Lista de ingredientes |
| `/ingredientes/nuevo` | Nuevo ingrediente |
| `/ingredientes/ver/{id}` | Ver ingrediente |
| `/ventas` | Lista de ventas |
| `/ventas/nueva` | Punto de venta (caja) |
| `/ventas/ver/{id}` | Ver detalle de venta |
| `/ventas/anular/{id}` | Anular venta |
| `/ventas/cierre-x` | Reporte cierre X |
| `/ventas/cierre-z` | Cierre Z (cierra el día) |
| `/compras` | Lista de compras |
| `/compras/nueva` | Nueva compra |
| `/compras/ver/{id}` | Ver compra |
| `/compras/anular/{id}` | Anular compra |
| `/ajuste-precios` | Ajuste masivo de precios |
| `/inventario` | Inventario físico |
| `/inventario/reporte` | Reporte de inventario |
| `/inventario/descargar-plantilla` | Descargar Excel para conteo |

## API Endpoints (JSON)

| Endpoint | Descripción |
|---------|-------------|
| `GET /recetas/stock/{id}` | Retorna unidades disponibles de una receta |
| `GET /productos/estimado/{id}` | Retorna stock estimado del producto |
| `GET /ventas/dia-cerrado` | Verifica si el día está cerrado |

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
6. **Cliente**: "Consumidor Final" se reutiliza si ya existe
7. **Día cerrado**: No permite ventas después de cierre Z

### Inventario
1. **Inventario Físico**: Registro diario del stock real
2. **Historial**: Se guarda cada registro con fecha
3. **Reporte**: Muestra flujo por período (inicial, consumo, final, diferencia)
4. **Plantilla Excel**: Descarga para facilitar conteo

### Superadmin
1. **Login separado**: `/superadmin/login` con credenciales propias
2. **Usuario**: `esSuperadmin = true`, sin empresa asignada
3. **Activación**: Al activar empresa, se crea el usuario admin
4. **Demo**: Empresa "restamodelo" precargada con datos de ejemplo

### Empresas
1. **Registro**: Se crean inactivas, esperan activación
2. **Activación**: Solo superadmin puede activar
3. **Subdominio**: Único por empresa, usado como tenant

## Excepciones Personalizadas

- **BusinessException**: Errores de negocio (validaciones, reglas)
- **DuplicateResourceException**: Recursos duplicados
- **GlobalExceptionHandler**: Manejo centralizado de excepciones con redirección a página anterior

## Licencia

MIT
