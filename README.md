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

### Gestión de Productos
- Productos con y sin receta
- Control de stock (solo productos sin receta)
- Precio de compra y venta con margen automático

### Gestión de Recetas
- Recetas asociadas a productos
- Ingredientes con cantidades
- Cálculo de costo automático

### Gestión de Ingredientes
- Catálogo de ingredientes
- Control de stock

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
├── exceptions/     # Excepciones personalizadas
├── config/         # Configuración
└── multitenant/    # Contexto de tenants
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
| `/productos` | Lista de productos |
| `/productos/nuevo` | Nuevo producto |
| `/recetas` | Lista de recetas |
| `/recetas/nueva` | Nueva receta |
| `/ingredientes` | Lista de ingredientes |

## Reglas de Negocio

1. **Productos con receta**: No manejan stock directo, se calcula por ingredientes
2. **Productos sin receta**: Manejan stock directamente, no pueden tener receta
3. **Eliminación**: Solo eliminación lógica (desactivación)
4. **Tipo de producto**: No se puede cambiar después de creado

## Licencia

MIT
