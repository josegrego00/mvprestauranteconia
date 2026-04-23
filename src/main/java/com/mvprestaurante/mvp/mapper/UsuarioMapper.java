package com.mvprestaurante.mvp.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.mvprestaurante.mvp.DTO.UsuarioRequestDTO;
import com.mvprestaurante.mvp.DTO.UsuarioResponseDTO;
import com.mvprestaurante.mvp.models.Usuario;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "empresa", ignore = true)
    @Mapping(target = "esSuperadmin", ignore = true)
    Usuario toEntity(UsuarioRequestDTO dto);

    @Mapping(target = "empresaId", expression = "java(usuario.getEmpresa() != null ? usuario.getEmpresa().getId() : null)")
    @Mapping(target = "nombreEmpresa", expression = "java(usuario.getEmpresa() != null ? usuario.getEmpresa().getNombreEmpresa() : null)")
    UsuarioResponseDTO toResponse(Usuario usuario);
}