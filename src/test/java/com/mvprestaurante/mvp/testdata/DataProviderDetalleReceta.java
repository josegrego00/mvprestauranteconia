package com.mvprestaurante.mvp.testdata;

import java.math.BigDecimal;
import java.util.List;

import com.mvprestaurante.mvp.DTO.DetalleRecetaDTO;
import com.mvprestaurante.mvp.enums.UnidadMedida;
import com.mvprestaurante.mvp.models.DetalleReceta;
import com.mvprestaurante.mvp.models.Empresa;
import com.mvprestaurante.mvp.models.Ingrediente;
import com.mvprestaurante.mvp.models.Receta;

public class DataProviderDetalleReceta {

    private static final Empresa EMPRESA_FICTICIA = Empresa.builder()
            .id(1L)
            .subdominio("testempresa")
            .nombreEmpresa("Test Empresa")
            .email("test@empresa.com")
            .telefono("1234567890")
            .plan("BASICO")
            .activa(true)
            .build();

    private static final Receta RECETA_FICTICIA = Receta.builder()
            .id(1L)
            .nombre("Hamburguesa clásica")
            .descripcion("Receta de hamburguesa")
            .precioBruto(BigDecimal.valueOf(6000.0))
            .precioVenta(BigDecimal.valueOf(12000.0))
            .estaActiva(true)
            .empresa(EMPRESA_FICTICIA)
            .build();

    private static final Ingrediente INGREDIENTE_FICTICIO = Ingrediente.builder()
            .id(1L)
            .nombre("Carne molida")
            .stockDisponible(50.0)
            .precioCompra(BigDecimal.valueOf(15000.0))
            .unidadMedida(UnidadMedida.KG)
            .estaActivo(true)
            .empresa(EMPRESA_FICTICIA)
            .build();

    public static DetalleReceta unDetalleReceta() {
        return DetalleReceta.builder()
                .nombre("Carne molida")
                .cantidadIngrediente(BigDecimal.valueOf(0.25))
                .build();
    }

    public static DetalleReceta unDetalleRecetaCompleto() {
        return DetalleReceta.builder()
                .id(1L)
                .nombre("Carne molida")
                .receta(RECETA_FICTICIA)
                .ingrediente(INGREDIENTE_FICTICIO)
                .cantidadIngrediente(BigDecimal.valueOf(0.25))
                .build();
    }

    public static DetalleReceta unDetalleRecetaConId() {
        return DetalleReceta.builder()
                .id(1L)
                .nombre("Carne molida")
                .receta(RECETA_FICTICIA)
                .ingrediente(INGREDIENTE_FICTICIO)
                .cantidadIngrediente(BigDecimal.valueOf(0.25))
                .build();
    }

    public static DetalleRecetaDTO unDetalleRecetaDTO() {
        return DetalleRecetaDTO.builder()
                .nombre("Carne molida")
                .ingredienteId(1L)
                .recetaId(1L)
                .cantidadIngrediente(BigDecimal.valueOf(0.25))
                .build();
    }

    public static DetalleRecetaDTO unDetalleRecetaDTOConId() {
        return DetalleRecetaDTO.builder()
                .id(1L)
                .nombre("Carne molida")
                .ingredienteId(1L)
                .ingredienteNombre("Carne molida")
                .ingredienteUnidadMedida("KG")
                .ingredienteStockDisponible(BigDecimal.valueOf(50.0))
                .recetaId(1L)
                .cantidadIngrediente(BigDecimal.valueOf(0.25))
                .build();
    }

    public static List<DetalleReceta> listaDetallesReceta() {
        return List.of(
                DetalleReceta.builder()
                        .id(1L)
                        .nombre("Carne molida")
                        .receta(RECETA_FICTICIA)
                        .ingrediente(INGREDIENTE_FICTICIO)
                        .cantidadIngrediente(BigDecimal.valueOf(0.25))
                        .build(),
                DetalleReceta.builder()
                        .id(2L)
                        .nombre("Queso cheddar")
                        .receta(RECETA_FICTICIA)
                        .ingrediente(Ingrediente.builder()
                                .id(2L)
                                .nombre("Queso cheddar")
                                .stockDisponible(30.0)
                                .precioCompra(BigDecimal.valueOf(12000.0))
                                .unidadMedida(UnidadMedida.KG)
                                .estaActivo(true)
                                .empresa(EMPRESA_FICTICIA)
                                .build())
                        .cantidadIngrediente(BigDecimal.valueOf(0.1))
                        .build()
        );
    }

    public static List<DetalleRecetaDTO> listaDetallesRecetaDTO() {
        return List.of(
                DetalleRecetaDTO.builder()
                        .id(1L)
                        .nombre("Carne molida")
                        .ingredienteId(1L)
                        .ingredienteNombre("Carne molida")
                        .ingredienteUnidadMedida("KG")
                        .ingredienteStockDisponible(BigDecimal.valueOf(50.0))
                        .recetaId(1L)
                        .cantidadIngrediente(BigDecimal.valueOf(0.25))
                        .build(),
                DetalleRecetaDTO.builder()
                        .id(2L)
                        .nombre("Queso cheddar")
                        .ingredienteId(2L)
                        .ingredienteNombre("Queso cheddar")
                        .ingredienteUnidadMedida("KG")
                        .ingredienteStockDisponible(BigDecimal.valueOf(30.0))
                        .recetaId(1L)
                        .cantidadIngrediente(BigDecimal.valueOf(0.1))
                        .build()
        );
    }
}