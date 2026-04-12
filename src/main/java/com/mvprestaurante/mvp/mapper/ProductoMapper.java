package com.mvprestaurante.mvp.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.mvprestaurante.mvp.DTO.ProductoDTO;
import com.mvprestaurante.mvp.models.Producto;

@Mapper(componentModel = "spring")
public interface ProductoMapper {

    @Mapping(target = "empresa", ignore = true)
    @Mapping(target = "receta", ignore = true)
    Producto toEntity(ProductoDTO dto);

    @Mapping(target = "empresaId", source = "empresa.id")
    @Mapping(target = "recetaId", source = "receta.id")
    @Mapping(target = "recetaNombre", source = "receta.nombre")
    @Mapping(target = "stockEstimado", ignore = true)
    ProductoDTO toDTO(Producto producto);

    default ProductoDTO toSimpleDTO(Producto producto) {
        return toDTO(producto);
    }
}