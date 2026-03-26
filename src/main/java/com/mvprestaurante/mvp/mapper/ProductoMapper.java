package com.mvprestaurante.mvp.mapper;

import org.mapstruct.Mapper;

import com.mvprestaurante.mvp.DTO.ProductoDTO;
import com.mvprestaurante.mvp.models.Producto;

@Mapper(componentModel = "spring")
public interface ProductoMapper {

    ProductoDTO toSimpleDTO(Producto producto);
}
