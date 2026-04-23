package com.mvprestaurante.mvp.testdata;

import java.math.BigDecimal;
import java.util.List;

import com.mvprestaurante.mvp.DTO.IngredienteDTO;
import com.mvprestaurante.mvp.enums.UnidadMedida;
import com.mvprestaurante.mvp.models.Empresa;
import com.mvprestaurante.mvp.models.Ingrediente;

public class DataProviderIngrediente {

    private static final Empresa EMPRESA_FICTICIA = Empresa.builder()
            .id(1L)
            .subdominio("testempresa")
            .nombreEmpresa("Test Empresa")
            .email("test@empresa.com")
            .telefono("1234567890")
            .plan("BASICO")
            .activa(true)
            .build();

    public static Ingrediente unIngrediente() {
        return Ingrediente.builder()
                .nombre("Carne molida")
                .stockDisponible(50.0)
                .precioCompra(BigDecimal.valueOf(15000.0))
                .unidadMedida(UnidadMedida.KG)
                .estaActivo(true)
                .build();
    }

    public static Ingrediente unIngredienteConId() {
        return Ingrediente.builder()
                .id(1L)
                .nombre("Carne molida")
                .stockDisponible(50.0)
                .precioCompra(BigDecimal.valueOf(15000.0))
                .unidadMedida(UnidadMedida.KG)
                .estaActivo(true)
                .empresa(EMPRESA_FICTICIA)
                .build();
    }

    public static Ingrediente unIngredienteActivo() {
        return Ingrediente.builder()
                .id(1L)
                .nombre("Queso")
                .stockDisponible(30.0)
                .precioCompra(BigDecimal.valueOf(12000.0))
                .unidadMedida(UnidadMedida.KG)
                .estaActivo(true)
                .empresa(EMPRESA_FICTICIA)
                .build();
    }

    public static Ingrediente unIngredienteInactivo() {
        return Ingrediente.builder()
                .id(2L)
                .nombre("Lechuga")
                .stockDisponible(20.0)
                .precioCompra(BigDecimal.valueOf(5000.0))
                .unidadMedida(UnidadMedida.KG)
                .estaActivo(false)
                .empresa(EMPRESA_FICTICIA)
                .build();
    }

    public static IngredienteDTO unIngredienteDTO() {
        return IngredienteDTO.builder()
                .nombre("Nuevo Ingrediente")
                .stockDisponible(25.0)
                .precioCompra(BigDecimal.valueOf(10000.0))
                .unidadMedida(UnidadMedida.KG)
                .estaActivo(true)
                .build();
    }

    public static IngredienteDTO unIngredienteDTOConId() {
        return IngredienteDTO.builder()
                .id(1L)
                .nombre("Ingrediente Actualizado")
                .stockDisponible(30.0)
                .precioCompra(BigDecimal.valueOf(12000.0))
                .unidadMedida(UnidadMedida.KG)
                .estaActivo(true)
                .build();
    }

    public static List<Ingrediente> listaIngredientes() {
        return List.of(
                Ingrediente.builder()
                        .id(1L)
                        .nombre("Carne molida")
                        .stockDisponible(50.0)
                        .precioCompra(BigDecimal.valueOf(15000.0))
                        .unidadMedida(UnidadMedida.KG)
                        .estaActivo(true)
                        .empresa(EMPRESA_FICTICIA)
                        .build(),
                Ingrediente.builder()
                        .id(2L)
                        .nombre("Queso")
                        .stockDisponible(30.0)
                        .precioCompra(BigDecimal.valueOf(12000.0))
                        .unidadMedida(UnidadMedida.KG)
                        .estaActivo(true)
                        .empresa(EMPRESA_FICTICIA)
                        .build()
        );
    }

    public static List<IngredienteDTO> listaIngredientesDTO() {
        return List.of(
                IngredienteDTO.builder()
                        .id(1L)
                        .nombre("Carne molida")
                        .stockDisponible(50.0)
                        .precioCompra(BigDecimal.valueOf(15000.0))
                        .unidadMedida(UnidadMedida.KG)
                        .estaActivo(true)
                        .empresaId(1L)
                        .build(),
                IngredienteDTO.builder()
                        .id(2L)
                        .nombre("Queso")
                        .stockDisponible(30.0)
                        .precioCompra(BigDecimal.valueOf(12000.0))
                        .unidadMedida(UnidadMedida.KG)
                        .estaActivo(true)
                        .empresaId(1L)
                        .build()
        );
    }
}