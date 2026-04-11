package com.mvprestaurante.mvp.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.mvprestaurante.mvp.DTO.IngredienteDTO;
import com.mvprestaurante.mvp.models.Ingrediente;

@Mapper(componentModel = "spring")
public interface IngredienteMapper {

    @Mapping(target = "empresa", ignore = true)
    Ingrediente toEntity(IngredienteDTO dto);

    @Mapping(target = "empresaId", source = "empresa.id")
    IngredienteDTO toDTO(Ingrediente ingrediente);
}