package com.mvprestaurante.mvp.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.mvprestaurante.mvp.DTO.EmpresaDTO;

import com.mvprestaurante.mvp.models.Empresa;

@Mapper(componentModel = "spring")
public interface EmpresaMapper {

    @Mapping(target = "id", ignore = true) // El ID se genera automáticamente
    Empresa toEntity(EmpresaDTO dto);

    EmpresaDTO toResponse(Empresa empresa);
    
}