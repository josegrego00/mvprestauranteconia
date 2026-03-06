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
    private EmpresaRepositorio empresaRepository;

    @Autowired
    private EmpresaMapper empresaMapper;

    @Transactional
    public EmpresaDTOResponse registrarEmpresa(EmpresaDTORequest dto) {

        // Validar que el subdominio no exista
        if (empresaRepository.existsBySubdominio(dto.getSubdominio())) {
            throw new RuntimeException("El subdominio '" + dto.getSubdominio() + "' ya está en uso");
        }

        // Convertir DTO a entidad usando el mapper
        Empresa empresa = empresaMapper.toEntity(dto);

        // Guardar en BD
        Empresa empresaGuardada = empresaRepository.save(empresa);

        // Convertir entidad a DTO de respuesta y retornar
        return empresaMapper.toResponse(empresaGuardada);
    }

    public boolean existePorSubdominio(String subdominio) {
        return empresaRepository.existsBySubdominio(subdominio);
    }

    public EmpresaDTOResponse buscarPorSubdominio(String subdominio) {
        Empresa empresa = empresaRepository.findBySubdominio(subdominio)
                .orElseThrow(() -> new RuntimeException("No existe empresa con subdominio: " + subdominio));
        return empresaMapper.toResponse(empresa);
    }
}