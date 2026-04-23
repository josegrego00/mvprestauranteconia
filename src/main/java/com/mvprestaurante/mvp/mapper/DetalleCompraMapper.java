package com.mvprestaurante.mvp.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ValueMapping;
import org.mapstruct.ValueMappings;

import com.mvprestaurante.mvp.DTO.DetalleCompraDTO;
import com.mvprestaurante.mvp.models.DetalleCompra;
import com.mvprestaurante.mvp.enums.TipoItem;

@Mapper(componentModel = "spring")
public interface DetalleCompraMapper {

    @Mapping(target = "compra", ignore = true)
    @Mapping(target = "ingrediente", ignore = true)
    @Mapping(target = "producto", ignore = true)
    @Mapping(target = "tipoItem", source = "tipoItem")
    DetalleCompra toEntity(DetalleCompraDTO dto);

    @Mapping(target = "ingredienteId", expression = "java(detalle.getIngrediente() != null ? detalle.getIngrediente().getId() : null)")
    @Mapping(target = "productoId", expression = "java(detalle.getProducto() != null ? detalle.getProducto().getId() : null)")
    DetalleCompraDTO toDTO(DetalleCompra detalle);
}