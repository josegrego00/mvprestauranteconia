package com.mvprestaurante.mvp.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.mvprestaurante.mvp.DTO.CompraDTO;
import com.mvprestaurante.mvp.models.Compra;

@Mapper(componentModel = "spring", uses = DetalleCompraMapper.class)
public interface CompraMapper {

    @Mapping(target = "empresa", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    @Mapping(target = "detallesCompra", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "numeroCompra", ignore = true)
    @Mapping(target = "fechaCompra", ignore = true)
    Compra toEntity(CompraDTO dto);

    @Mapping(target = "empresaId", source = "empresa.id")
    @Mapping(target = "nombreUsuario", source = "usuario.nombre")
    CompraDTO toDTO(Compra compra);
}