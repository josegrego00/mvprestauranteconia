package com.mvprestaurante.mvp.mapper;

import org.mapstruct.Mapper;

import com.mvprestaurante.mvp.DTO.UsuarioDTORequest;
import com.mvprestaurante.mvp.DTO.UsuarioDTOResponse;
import com.mvprestaurante.mvp.models.Usuario;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    Usuario toEntity(UsuarioDTORequest dto);

    UsuarioDTOResponse toResponse(Usuario usuario);
    
}
