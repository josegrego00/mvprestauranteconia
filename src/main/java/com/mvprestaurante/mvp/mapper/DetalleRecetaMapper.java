package com.mvprestaurante.mvp.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.mvprestaurante.mvp.DTO.DetalleRecetaDTO;
import com.mvprestaurante.mvp.models.DetalleReceta;

@Mapper(componentModel = "spring", uses = {})
public interface DetalleRecetaMapper {

    @Mapping(target = "receta", ignore = true)
    @Mapping(target = "ingrediente", ignore = true)
    @Mapping(target = "id", ignore = true)
    DetalleReceta toEntity(DetalleRecetaDTO dto);

    @Mapping(target = "recetaId", expression = "java(detalleReceta.getReceta() != null ? detalleReceta.getReceta().getId() : null)")
    @Mapping(target = "ingredienteId", source = "ingrediente.id")
    @Mapping(target = "ingredienteNombre", source = "ingrediente.nombre")
    @Mapping(target = "ingredienteUnidadMedida", expression = "java(detalleReceta.getIngrediente() != null && detalleReceta.getIngrediente().getUnidadMedida() != null ? detalleReceta.getIngrediente().getUnidadMedida().name() : null)")
    @Mapping(target = "ingredienteStockDisponible", source = "ingrediente.stockDisponible")
    DetalleRecetaDTO toDTO(DetalleReceta detalleReceta);
}