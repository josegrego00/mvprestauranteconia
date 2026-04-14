package com.mvprestaurante.mvp.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.mvprestaurante.mvp.DTO.VentaDTO;
import com.mvprestaurante.mvp.models.Venta;

@Mapper(componentModel = "spring")
public interface VentaMapper {

    @Mapping(target = "cliente", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    @Mapping(target = "empresa", ignore = true)
    @Mapping(target = "detallesVenta", ignore = true)
    Venta toEntity(VentaDTO dto);

    @Mapping(target = "clienteId", source = "cliente.id")
    @Mapping(target = "nombreCliente", source = "cliente.nombre")
    @Mapping(target = "usuarioId", source = "usuario.id")
    @Mapping(target = "nombreUsuario", source = "usuario.nombre")
    @Mapping(target = "detalles", source = "detallesVenta")
    VentaDTO toDTO(Venta venta);
}