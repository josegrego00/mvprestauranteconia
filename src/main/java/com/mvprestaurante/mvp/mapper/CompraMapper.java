package com.mvprestaurante.mvp.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.mvprestaurante.mvp.DTO.CompraDetalleDTO;
import com.mvprestaurante.mvp.DTO.DetalleCompraDTO;
import com.mvprestaurante.mvp.models.Compra;
import com.mvprestaurante.mvp.models.DetalleCompra;

@Mapper(componentModel = "spring")
public interface CompraMapper {

    @Mapping(target = "nombreUsuario", source = "usuario.nombre")
    @Mapping(target = "detalles", source = "detallesCompra")
    CompraDetalleDTO toDetalleDTO(Compra compra);

    @Mapping(target = "nombreItem", expression = "java(getNombreItem(detalle))")
    DetalleCompraDTO toDetalleDTO(DetalleCompra detalle);

    default String getNombreItem(DetalleCompra detalle) {
        if (detalle == null) return "";
        if ("INGREDIENTE".equals(detalle.getTipoItem()) && detalle.getIngrediente() != null) {
            return detalle.getIngrediente().getNombre();
        } else if ("PRODUCTO".equals(detalle.getTipoItem()) && detalle.getProducto() != null) {
            return detalle.getProducto().getNombre();
        }
        return "";
    }
}
