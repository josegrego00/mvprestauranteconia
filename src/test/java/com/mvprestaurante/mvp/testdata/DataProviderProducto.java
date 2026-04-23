package com.mvprestaurante.mvp.testdata;

import java.math.BigDecimal;
import java.util.List;

import com.mvprestaurante.mvp.DTO.ProductoDTO;
import com.mvprestaurante.mvp.models.Empresa;
import com.mvprestaurante.mvp.models.Producto;
import com.mvprestaurante.mvp.models.Receta;

public class DataProviderProducto {

    private static final Empresa EMPRESA_FICTICIA = Empresa.builder()
            .id(1L)
            .subdominio("testempresa")
            .nombreEmpresa("Test Empresa")
            .email("test@empresa.com")
            .telefono("1234567890")
            .plan("BASICO")
            .activa(true)
            .build();

    public static Producto unProducto() {
        return Producto.builder()
                .nombre("Hamburguesa clásica")
                .descripcion("Deliciosa hamburguesa con todos los toppings")
                .precioVenta(BigDecimal.valueOf(12000.0))
                .precioCompra(BigDecimal.valueOf(6000.0))
                .tieneReceta(false)
                .estaActivo(true)
                .stock(50.0)
                .build();
    }

    public static Producto unProductoConId() {
        return Producto.builder()
                .id(1L)
                .nombre("Hamburguesa clásica")
                .descripcion("Deliciosa hamburguesa con todos los toppings")
                .precioVenta(BigDecimal.valueOf(12000.0))
                .precioCompra(BigDecimal.valueOf(6000.0))
                .tieneReceta(false)
                .estaActivo(true)
                .stock(50.0)
                .empresa(EMPRESA_FICTICIA)
                .build();
    }

    public static Producto unProductoActivo() {
        return Producto.builder()
                .id(1L)
                .nombre("Pizza mediana")
                .descripcion("Pizza con jamón y queso")
                .precioVenta(BigDecimal.valueOf(15000.0))
                .precioCompra(BigDecimal.valueOf(8000.0))
                .tieneReceta(true)
                .estaActivo(true)
                .empresa(EMPRESA_FICTICIA)
                .build();
    }

    public static Producto unProductoInactivo() {
        return Producto.builder()
                .id(2L)
                .nombre("Pizza grande")
                .descripcion("Pizza grande con múltiples toppings")
                .precioVenta(BigDecimal.valueOf(25000.0))
                .precioCompra(BigDecimal.valueOf(12000.0))
                .tieneReceta(true)
                .estaActivo(false)
                .empresa(EMPRESA_FICTICIA)
                .build();
    }

    public static ProductoDTO unProductoDTO() {
        return ProductoDTO.builder()
                .nombre("Nuevo Producto")
                .descripcion("Descripción del producto")
                .precioVenta(BigDecimal.valueOf(10000.0))
                .precioCompra(BigDecimal.valueOf(5000.0))
                .tieneReceta(false)
                .estaActivo(true)
                .stock(30.0)
                .build();
    }

    public static ProductoDTO unProductoDTOConId() {
        return ProductoDTO.builder()
                .id(1L)
                .nombre("Producto Actualizado")
                .descripcion("Descripción actualizada")
                .precioVenta(BigDecimal.valueOf(15000.0))
                .precioCompra(BigDecimal.valueOf(7000.0))
                .tieneReceta(false)
                .estaActivo(true)
                .stock(20.0)
                .build();
    }

    public static List<Producto> listaProductos() {
        return List.of(
                Producto.builder()
                        .id(1L)
                        .nombre("Hamburguesa clásica")
                        .descripcion("Deliciosa hamburguesa")
                        .precioVenta(BigDecimal.valueOf(12000.0))
                        .precioCompra(BigDecimal.valueOf(6000.0))
                        .tieneReceta(false)
                        .estaActivo(true)
                        .stock(50.0)
                        .empresa(EMPRESA_FICTICIA)
                        .build(),
                Producto.builder()
                        .id(2L)
                        .nombre("Pizza mediana")
                        .descripcion("Pizza con jamón y queso")
                        .precioVenta(BigDecimal.valueOf(15000.0))
                        .precioCompra(BigDecimal.valueOf(8000.0))
                        .tieneReceta(true)
                        .estaActivo(true)
                        .empresa(EMPRESA_FICTICIA)
                        .build()
        );
    }

    public static List<ProductoDTO> listaProductosDTO() {
        return List.of(
                ProductoDTO.builder()
                        .id(1L)
                        .nombre("Hamburguesa clásica")
                        .descripcion("Deliciosa hamburguesa")
                        .precioVenta(BigDecimal.valueOf(12000.0))
                        .precioCompra(BigDecimal.valueOf(6000.0))
                        .tieneReceta(false)
                        .estaActivo(true)
                        .stock(50.0)
                        .empresaId(1L)
                        .build(),
                ProductoDTO.builder()
                        .id(2L)
                        .nombre("Pizza mediana")
                        .descripcion("Pizza con jamón y queso")
                        .precioVenta(BigDecimal.valueOf(15000.0))
                        .precioCompra(BigDecimal.valueOf(8000.0))
                        .tieneReceta(true)
                        .estaActivo(true)
                        .empresaId(1L)
                        .build()
        );
    }
}