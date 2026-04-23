package com.mvprestaurante.mvp.testdata;

import java.math.BigDecimal;
import java.util.List;

import com.mvprestaurante.mvp.DTO.EmpresaDTO;
import com.mvprestaurante.mvp.models.Empresa;

public class DataProviderEmpresa {

    public static Empresa unaEmpresa() {
        return Empresa.builder()
                .subdominio("testEmpresa")
                .nombreEmpresa("Test Empresa")
                .email("test@empresa.com")
                .telefono("1234567890")
                .plan("BASICO")
                .activa(false)
                .build();
    }

    public static Empresa unaEmpresaConId() {
        return Empresa.builder()
                .id(1L)
                .subdominio("testEmpresa")
                .nombreEmpresa("Test Empresa")
                .email("test@empresa.com")
                .telefono("1234567890")
                .plan("BASICO")
                .activa(false)
                .build();
    }

    public static Empresa unaEmpresaInactiva() {
        return Empresa.builder()
                .id(1L)
                .subdominio("inactiva")
                .nombreEmpresa("Empresa Inactiva")
                .email("inactiva@test.com")
                .telefono("0000000000")
                .plan("BASICO")
                .activa(false)
                .build();
    }

    public static Empresa unaEmpresaActiva() {
        return Empresa.builder()
                .id(1L)
                .subdominio("activa")
                .nombreEmpresa("Empresa Activa")
                .email("activa@test.com")
                .telefono("1111111111")
                .plan("BASICO")
                .activa(true)
                .build();
    }

    public static EmpresaDTO unEmpresaDTO() {
        return EmpresaDTO.builder()
                .subdominio("nuevaEmpresa")
                .nombreEmpresa("Nueva Empresa")
                .email("nueva@empresa.com")
                .telefono("9876543210")
                .plan("PREMIUM")
                .activa(false)
                .build();
    }

    public static EmpresaDTO unEmpresaDTOConId() {
        return EmpresaDTO.builder()
                .id(1L)
                .subdominio("testEmpresa")
                .nombreEmpresa("Empresa Actualizada")
                .email("actualizado@test.com")
                .telefono("5555555555")
                .plan("PREMIUM")
                .activa(true)
                .build();
    }

    public static List<Empresa> listaEmpresas() {
        return List.of(
                Empresa.builder()
                        .id(1L)
                        .subdominio("empresa1")
                        .nombreEmpresa("Empresa 1")
                        .email("empresa1@test.com")
                        .telefono("1111111111")
                        .plan("BASICO")
                        .activa(true)
                        .build(),
                Empresa.builder()
                        .id(2L)
                        .subdominio("empresa2")
                        .nombreEmpresa("Empresa 2")
                        .email("empresa2@test.com")
                        .telefono("2222222222")
                        .plan("PREMIUM")
                        .activa(false)
                        .build()
        );
    }

    public static List<EmpresaDTO> listaEmpresasDTO() {
        return List.of(
                EmpresaDTO.builder()
                        .id(1L)
                        .subdominio("empresa1")
                        .nombreEmpresa("Empresa 1")
                        .email("empresa1@test.com")
                        .telefono("1111111111")
                        .plan("BASICO")
                        .activa(true)
                        .build(),
                EmpresaDTO.builder()
                        .id(2L)
                        .subdominio("empresa2")
                        .nombreEmpresa("Empresa 2")
                        .email("empresa2@test.com")
                        .telefono("2222222222")
                        .plan("PREMIUM")
                        .activa(false)
                        .build()
        );
    }
}