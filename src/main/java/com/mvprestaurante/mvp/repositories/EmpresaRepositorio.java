package com.mvprestaurante.mvp.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mvprestaurante.mvp.models.Empresa;

public interface EmpresaRepositorio extends JpaRepository<Empresa, Long> {

    Optional<Empresa> findBySubdominio(String subdominio);

    boolean existsBySubdominio(String subdominio);

}
