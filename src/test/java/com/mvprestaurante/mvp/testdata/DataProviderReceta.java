package com.mvprestaurante.mvp.testdata;

import java.math.BigDecimal;
import java.util.List;

import com.mvprestaurante.mvp.DTO.RecetaDTO;
import com.mvprestaurante.mvp.models.DetalleReceta;
import com.mvprestaurante.mvp.models.Empresa;
import com.mvprestaurante.mvp.models.Ingrediente;
import com.mvprestaurante.mvp.models.Receta;

import com.mvprestaurante.mvp.enums.UnidadMedida;

public class DataProviderReceta {

    private static final Empresa EMPRESA_FICTICIA = Empresa.builder()
            .id(1L)
            .subdominio("testempresa")
            .nombreEmpresa("Test Empresa")
            .email("test@empresa.com")
            .telefono("1234567890")
            .plan("BASICO")
            .activa(true)
            .build();

    public static Receta unaReceta() {
        return Receta.builder()
                .nombre("Hamburguesa clásica")
                .descripcion("Receta de hamburguesa tradicional")
                .precioBruto(BigDecimal.valueOf(6000.0))
                .precioVenta(BigDecimal.valueOf(12000.0))
                .estaActiva(true)
                .build();
    }

    public static Receta unaRecetaCompleta() {
        return Receta.builder()
                .id(1L)
                .nombre("Hamburguesa clásica")
                .descripcion("Receta de hamburguesa tradicional")
                .precioBruto(BigDecimal.valueOf(6000.0))
                .precioVenta(BigDecimal.valueOf(12000.0))
                .estaActiva(true)
                .empresa(EMPRESA_FICTICIA)
                .build();
    }

    public static Receta unaRecetaConId() {
        return Receta.builder()
                .id(1L)
                .nombre("Hamburguesa clásica")
                .descripcion("Receta de hamburguesa")
                .precioBruto(BigDecimal.valueOf(6000.0))
                .precioVenta(BigDecimal.valueOf(12000.0))
                .estaActiva(true)
                .empresa(EMPRESA_FICTICIA)
                .build();
    }

    public static RecetaDTO unaRecetaDTO() {
        return RecetaDTO.builder()
                .nombre("Hamburguesa clásica")
                .descripcion("Receta de hamburguesa tradicional")
                .precioVenta(BigDecimal.valueOf(12000.0))
                .estaActiva(true)
                .build();
    }

    public static RecetaDTO unaRecetaDTOConId() {
        return RecetaDTO.builder()
                .id(1L)
                .nombre("Hamburguesa clásica")
                .descripcion("Receta de hamburguesa")
                .precioBruto(BigDecimal.valueOf(6000.0))
                .precioVenta(BigDecimal.valueOf(12000.0))
                .estaActiva(true)
                .empresaId(1L)
                .build();
    }

    public static List<Receta> listaRecetas() {
        return List.of(
                Receta.builder()
                        .id(1L)
                        .nombre("Hamburguesa clásica")
                        .descripcion("Receta tradicional")
                        .precioBruto(BigDecimal.valueOf(6000.0))
                        .precioVenta(BigDecimal.valueOf(12000.0))
                        .estaActiva(true)
                        .empresa(EMPRESA_FICTICIA)
                        .build(),
                Receta.builder()
                        .id(2L)
                        .nombre("Pizza margarita")
                        .descripcion("Pizza italiana")
                        .precioBruto(BigDecimal.valueOf(8000.0))
                        .precioVenta(BigDecimal.valueOf(15000.0))
                        .estaActiva(true)
                        .empresa(EMPRESA_FICTICIA)
                        .build()
        );
    }

    public static List<RecetaDTO> listaRecetasDTO() {
        return List.of(
                RecetaDTO.builder()
                        .id(1L)
                        .nombre("Hamburguesa clásica")
                        .descripcion("Receta tradicional")
                        .precioBruto(BigDecimal.valueOf(6000.0))
                        .precioVenta(BigDecimal.valueOf(12000.0))
                        .estaActiva(true)
                        .empresaId(1L)
                        .build(),
                RecetaDTO.builder()
                        .id(2L)
                        .nombre("Pizza margarita")
                        .descripcion("Pizza italiana")
                        .precioBruto(BigDecimal.valueOf(8000.0))
                        .precioVenta(BigDecimal.valueOf(15000.0))
                        .estaActiva(true)
                        .empresaId(1L)
                        .build()
        );
    }
}