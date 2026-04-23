package com.mvprestaurante.mvp.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.mvprestaurante.mvp.DTO.RecetaDTO;
import com.mvprestaurante.mvp.models.Receta;

@Mapper(componentModel = "spring", uses = DetalleRecetaMapper.class)
public interface RecetaMapper {

    @Mapping(target = "empresa", ignore = true)
    @Mapping(target = "producto", ignore = true)
    @Mapping(target = "listaIngredientes", ignore = true)
    @Mapping(target = "id", ignore = true)
    Receta toEntity(RecetaDTO dto);

    @Mapping(target = "empresaId", source = "empresa.id")
    @Mapping(target = "productoId", source = "producto.id")
    @Mapping(target = "productoNombre", source = "producto.nombre")
    RecetaDTO toDTO(Receta receta);
}