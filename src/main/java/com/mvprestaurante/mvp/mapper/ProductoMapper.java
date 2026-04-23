package com.mvprestaurante.mvp.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.mvprestaurante.mvp.DTO.ProductoDTO;
import com.mvprestaurante.mvp.models.Producto;

@Mapper(componentModel = "spring")
public interface ProductoMapper {

    @Mapping(target = "empresa", ignore = true)
    @Mapping(target = "receta", ignore = true)
    @Mapping(target = "id", ignore = true)
    Producto toEntity(ProductoDTO dto);

    @Mapping(target = "empresaId", source = "empresa.id")
    @Mapping(target = "recetaId", expression = "java(producto.getReceta() != null ? producto.getReceta().getId() : null)")
    @Mapping(target = "recetaNombre", expression = "java(producto.getReceta() != null ? producto.getReceta().getNombre() : null)")
    @Mapping(target = "precioBruto", expression = "java(producto.getReceta() != null ? producto.getReceta().getPrecioBruto() : null)")
    @Mapping(target = "stockEstimado", ignore = true)
    ProductoDTO toDTO(Producto producto);
}