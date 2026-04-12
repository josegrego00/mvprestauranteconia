package com.mvprestaurante.mvp.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.mvprestaurante.mvp.DTO.DetalleRecetaDTO;
import com.mvprestaurante.mvp.models.DetalleReceta;

@Mapper(componentModel = "spring")
public interface DetalleRecetaMapper {

    @Mapping(target = "receta", ignore = true)
    @Mapping(target = "ingrediente", ignore = true)
    DetalleReceta toEntity(DetalleRecetaDTO dto);

    @Mapping(target = "recetaId", source = "receta.id")
    @Mapping(target = "ingredienteId", source = "ingrediente.id")
    @Mapping(target = "ingredienteNombre", source = "ingrediente.nombre")
    @Mapping(target = "ingredienteUnidadMedida", source = "ingrediente.unidadMedida")
    @Mapping(target = "ingredienteStockDisponible", source = "ingrediente.stockDisponible")
    DetalleRecetaDTO toDTO(DetalleReceta detalleReceta);
}