package com.mvprestaurante.mvp.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.mvprestaurante.mvp.DTO.UsuarioDTORequest;
import com.mvprestaurante.mvp.DTO.UsuarioDTOResponse;
import com.mvprestaurante.mvp.models.Usuario;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "empresa", ignore = true)
    @Mapping(target = "esSuperadmin", ignore = true)
    Usuario toEntityFromRequest(UsuarioDTORequest dto);

    @Mapping(target = "empresaId", source = "empresa.id")
    @Mapping(target = "nombreEmpresa", source = "empresa.nombreEmpresa")
    UsuarioDTOResponse toResponse(Usuario usuario);
}