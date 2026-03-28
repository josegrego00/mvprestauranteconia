package com.mvprestaurante.mvp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mvprestaurante.mvp.models.Cliente;

import java.util.Optional;

@Repository
public interface ClienteRepositorio extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByNombreContainingIgnoreCase(String nombre);
    
    Optional<Cliente> findByNombreContainingIgnoreCaseAndEmpresaId(String nombre, Long empresaId);
}
