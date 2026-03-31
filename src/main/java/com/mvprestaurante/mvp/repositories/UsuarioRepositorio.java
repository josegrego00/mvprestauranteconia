package com.mvprestaurante.mvp.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mvprestaurante.mvp.models.Usuario;

@Repository
public interface UsuarioRepositorio extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findBynombreUsuario(String nombreUsuario);

    Optional<Usuario> findByNombreUsuarioAndEmpresa_Id(String username, Long empresaId);

    List<Usuario> findByEmpresaId(Long empresaId);

    Optional<Usuario> findByIdAndEmpresaId(Long id, Long empresaId);

}
