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
    @Mapping(target = "cierreDia", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    MovimientoStock toEntity(MovimientoStockDTO dto);

    @Mapping(target = "empresaId", source = "empresa.id")
    @Mapping(target = "ingredienteId", expression = "java(movimiento.getIngrediente() != null ? movimiento.getIngrediente().getId() : null)")
    @Mapping(target = "nombreIngrediente", expression = "java(movimiento.getIngrediente() != null ? movimiento.getIngrediente().getNombre() : null)")
    @Mapping(target = "productoId", expression = "java(movimiento.getProducto() != null ? movimiento.getProducto().getId() : null)")
    @Mapping(target = "nombreProducto", expression = "java(movimiento.getProducto() != null ? movimiento.getProducto().getNombre() : null)")
    @Mapping(target = "ventaId", expression = "java(movimiento.getVenta() != null ? movimiento.getVenta().getId() : null)")
    @Mapping(target = "compraId", expression = "java(movimiento.getCompra() != null ? movimiento.getCompra().getId() : null)")
    @Mapping(target = "cierreDiaId", expression = "java(movimiento.getCierreDia() != null ? movimiento.getCierreDia().getId() : null)")
    @Mapping(target = "usuarioId", source = "usuario.id")
    @Mapping(target = "nombreUsuario", source = "usuario.nombre")
    MovimientoStockDTO toDTO(MovimientoStock movimiento);
}