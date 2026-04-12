package com.mvprestaurante.mvp.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.mvprestaurante.mvp.DTO.CompraDTO;
import com.mvprestaurante.mvp.models.Compra;

@Mapper(componentModel = "spring")
public interface CompraMapper {

    @Mapping(target = "empresa", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    @Mapping(target = "detallesCompra", ignore = true)
    Compra toEntity(CompraDTO dto);

    @Mapping(target = "empresaId", source = "empresa.id")
    @Mapping(target = "nombreUsuario", source = "usuario.nombre")
    CompraDTO toDTO(Compra compra);
}