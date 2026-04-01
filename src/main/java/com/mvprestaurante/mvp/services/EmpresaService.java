package com.mvprestaurante.mvp.services;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mvprestaurante.mvp.DTO.EmpresaDTO;
import com.mvprestaurante.mvp.exceptions.BusinessException;
import com.mvprestaurante.mvp.mapper.EmpresaMapper;
import com.mvprestaurante.mvp.models.Empresa;
import com.mvprestaurante.mvp.repositories.EmpresaRepositorio;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmpresaService {

    private final EmpresaRepositorio empresaRepositorio;
    private final UsuarioService usuarioService;
    private final EmpresaMapper empresaMapper;

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('SUPERADMIN')")
    public List<Empresa> listarTodas() {
        return empresaRepositorio.findAll();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('SUPERADMIN')")
    public Empresa buscarPorId(Long id) {
        return empresaRepositorio.findById(id)
                .orElseThrow(() -> new BusinessException("Empresa no encontrada"));
    }

    @Transactional
    public EmpresaDTO registrarEmpresa(EmpresaDTO dto) {
        if (empresaRepositorio.existsBySubdominio(dto.getSubdominio())) {
            throw new BusinessException("El subdominio '" + dto.getSubdominio() + "' ya está en uso");
        }

        Empresa empresa = empresaMapper.toEntity(dto);
        empresa.setActiva(false);
        Empresa empresaGuardada = empresaRepositorio.save(empresa);

        return empresaMapper.toResponse(empresaGuardada);
    }

    @Transactional
    @PreAuthorize("hasRole('SUPERADMIN')")
    public Empresa guardar(Empresa empresa) {
        if (empresa.getSubdominio() == null || empresa.getSubdominio().trim().isEmpty()) {
            throw new BusinessException("El subdominio es obligatorio");
        }
        
        if (empresaRepositorio.findBySubdominio(empresa.getSubdominio().toLowerCase()).isPresent()) {
            throw new BusinessException("El subdominio ya existe");
        }
        
        empresa.setSubdominio(empresa.getSubdominio().toLowerCase().trim());
        
        empresa.setActiva(false);
        
        return empresaRepositorio.save(empresa);
    }

    @Transactional
    @PreAuthorize("hasRole('SUPERADMIN')")
    public Empresa actualizar(Long id, Empresa empresaActualizada) {
        Empresa empresa = empresaRepositorio.findById(id)
                .orElseThrow(() -> new BusinessException("Empresa no encontrada"));
        
        empresa.setNombreEmpresa(empresaActualizada.getNombreEmpresa());
        empresa.setEmail(empresaActualizada.getEmail());
        empresa.setTelefono(empresaActualizada.getTelefono());
        empresa.setPlan(empresaActualizada.getPlan());
        empresa.setActiva(empresaActualizada.getActiva());
        
        return empresaRepositorio.save(empresa);
    }

    @Transactional
    @PreAuthorize("hasRole('SUPERADMIN')")
    public void eliminar(Long id) {
        Empresa empresa = empresaRepositorio.findById(id)
                .orElseThrow(() -> new BusinessException("Empresa no encontrada"));
        
        empresa.setActiva(false);
        empresaRepositorio.save(empresa);
    }

    @Transactional(readOnly = true)
    public boolean existeSubdominio(String subdominio) {
        return empresaRepositorio.findBySubdominio(subdominio.toLowerCase()).isPresent();
    }

    @Transactional(readOnly = true)
    public Empresa buscarPorSubdominio(String subdominio) {
        return empresaRepositorio.findBySubdominio(subdominio.toLowerCase()).orElse(null);
    }
}
