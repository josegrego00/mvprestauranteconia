package com.mvprestaurante.mvp.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.mvprestaurante.mvp.DTO.CompraDetalleDTO;
import com.mvprestaurante.mvp.models.DetalleCompra;

@Mapper(componentModel = "spring")
public interface DetalleCompraMapper {

    @Mapping(target = "compra", ignore = true)
    @Mapping(target = "ingrediente", ignore = true)
    @Mapping(target = "producto", ignore = true)
    DetalleCompra toEntity(CompraDetalleDTO dto);

    @Mapping(target = "itemId", ignore = true)
    @Mapping(target = "itemNombre", ignore = true)
    @Mapping(target = "compraId", source = "compra.id")
    CompraDetalleDTO toDTO(DetalleCompra detalle);
}