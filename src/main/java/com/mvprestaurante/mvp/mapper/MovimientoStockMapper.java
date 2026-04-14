package com.mvprestaurante.mvp.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.mvprestaurante.mvp.DTO.MovimientoStockDTO;
import com.mvprestaurante.mvp.models.MovimientoStock;

@Mapper(componentModel = "spring")
public interface MovimientoStockMapper {

    @Mapping(target = "empresa", ignore = true)
    @Mapping(target = "ingrediente", ignore = true)
    @Mapping(target = "producto", ignore = true)
    @Mapping(target = "venta", ignore = true)
    @Mapping(target = "compra", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    MovimientoStock toEntity(MovimientoStockDTO dto);

    @Mapping(target = "empresaId", source = "empresa.id")
    @Mapping(target = "ingredienteId", source = "ingrediente.id")
    @Mapping(target = "nombreIngrediente", source = "ingrediente.nombre")
    @Mapping(target = "productoId", source = "producto.id")
    @Mapping(target = "nombreProducto", source = "producto.nombre")
    @Mapping(target = "ventaId", source = "venta.id")
    @Mapping(target = "compraId", source = "compra.id")
    @Mapping(target = "usuarioId", source = "usuario.id")
    @Mapping(target = "nombreUsuario", source = "usuario.nombre")
    MovimientoStockDTO toDTO(MovimientoStock movimiento);
}