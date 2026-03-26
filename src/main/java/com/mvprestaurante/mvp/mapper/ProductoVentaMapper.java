package com.mvprestaurante.mvp.mapper;

import org.mapstruct.Mapper;

import com.mvprestaurante.mvp.DTO.ProductoVentaDTO;
import com.mvprestaurante.mvp.models.Producto;

@Mapper(componentModel = "spring")
public interface ProductoVentaMapper {
    ProductoVentaDTO toVentaDTO(Producto producto);
}
