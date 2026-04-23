package com.mvprestaurante.mvp.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.mvprestaurante.mvp.DTO.DetalleVentaDTO;
import com.mvprestaurante.mvp.models.DetalleVenta;

@Mapper(componentModel = "spring")
public interface DetalleVentaMapper {

    @Mapping(target = "venta", ignore = true)
    @Mapping(target = "producto", ignore = true)
    @Mapping(target = "id", ignore = true)
    DetalleVenta toEntity(DetalleVentaDTO dto);

    @Mapping(target = "productoId", source = "producto.id")
    @Mapping(target = "nombreProducto", source = "producto.nombre")
    DetalleVentaDTO toDTO(DetalleVenta detalle);
}