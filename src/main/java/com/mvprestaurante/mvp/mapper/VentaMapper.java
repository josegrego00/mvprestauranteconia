package com.mvprestaurante.mvp.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.mvprestaurante.mvp.DTO.DetalleVentaDTO;
import com.mvprestaurante.mvp.DTO.VentaDTO;
import com.mvprestaurante.mvp.models.DetalleVenta;
import com.mvprestaurante.mvp.models.Venta;

@Mapper(componentModel = "spring")
public interface VentaMapper {

    @Mapping(target = "nombreCliente", source = "cliente.nombre")
    @Mapping(target = "nombreUsuario", source = "usuario.nombre")
    @Mapping(target = "detalles", source = "detallesVenta")
    VentaDTO toDetalleDTO(Venta venta);

    @Mapping(target = "nombreProducto", source = "producto.nombre")
    DetalleVentaDTO toDetalleDTO(DetalleVenta detalleVenta);
}
