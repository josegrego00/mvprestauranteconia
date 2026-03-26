package com.mvprestaurante.mvp.mapper;

import org.mapstruct.Mapper;

import com.mvprestaurante.mvp.DTO.IngredienteDTO;
import com.mvprestaurante.mvp.models.Ingrediente;

@Mapper(componentModel = "spring")
public interface IngredienteMapper {

    IngredienteDTO toSimpleDTO(Ingrediente ingrediente);
}
