package com.mvprestaurante.mvp.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mvprestaurante.mvp.DTO.EmpresaDTORequest;
import com.mvprestaurante.mvp.DTO.EmpresaDTOResponse;
import com.mvprestaurante.mvp.mapper.EmpresaMapper;
import com.mvprestaurante.mvp.models.Empresa;
import com.mvprestaurante.mvp.repositories.EmpresaRepositorio;

@Service
public class EmpresaService {

    @Autowired
    private EmpresaRepositorio empresaRepositorio;
    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private EmpresaMapper empresaMapper;

    @Transactional
    public EmpresaDTOResponse registrarEmpresa(EmpresaDTORequest dto) {

        // Validar que el subdominio no exista
        if (empresaRepositorio.existsBySubdominio(dto.getSubdominio())) {
            throw new RuntimeException("El subdominio '" + dto.getSubdominio() + "' ya está en uso");
        }

        // 1. Guardar la empresa
        Empresa empresa = empresaMapper.toEntity(dto);
        Empresa empresaGuardada = empresaRepositorio.save(empresa);
        usuarioService.crearUsuarioAdmin(empresa);

        // 5. Convertir a DTO de respuesta
        return empresaMapper.toResponse(empresaGuardada);
    }

}