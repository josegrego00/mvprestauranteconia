package com.mvprestaurante.mvp.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.mvprestaurante.mvp.DTO.EmpresaDTO;
import com.mvprestaurante.mvp.models.Empresa;

@Mapper(componentModel = "spring")
public interface EmpresaMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "listaUsuario", ignore = true)
    @Mapping(target = "listaIngredientes", ignore = true)
    @Mapping(target = "listaRecetas", ignore = true)
    @Mapping(target = "listaProductos", ignore = true)
    Empresa toEntity(EmpresaDTO dto);

    EmpresaDTO toResponse(Empresa empresa);
}