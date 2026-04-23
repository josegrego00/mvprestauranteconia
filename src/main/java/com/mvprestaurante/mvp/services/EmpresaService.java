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
import com.mvprestaurante.mvp.utils.AuditLogger;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmpresaService {

    private final EmpresaRepositorio empresaRepositorio;
    private final EmpresaMapper empresaMapper;
    private final AuditLogger auditLogger;

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMINDEV')")
    public List<EmpresaDTO> listarTodas() {
        List<EmpresaDTO> empresas = empresaRepositorio.findAll().stream()
                .map(empresaMapper::toResponse)
                .toList();
        auditLogger.logListar("Empresa", empresas.size());
        return empresas;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMINDEV')")
    public EmpresaDTO buscarPorId(Long id) {
        Empresa empresa = empresaRepositorio.findById(id)
                .orElseThrow(() -> new BusinessException("Empresa no encontrada"));
        auditLogger.logBuscar("Empresa", String.valueOf(id));
        return empresaMapper.toResponse(empresa);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMINDEV')")
    public EmpresaDTO guardar(EmpresaDTO dto) {

        if (existeSubdominio(dto.getSubdominio()) || existeNombreEmpresa(dto.getNombreEmpresa())) {
            auditLogger.logError("CREAR", "Empresa", "Subdominio duplicado: " + dto.getSubdominio());
            throw new BusinessException("El subdominio o Nombre de la empresa ya existe");
        }

        Empresa empresa = empresaMapper.toEntity(dto);
        empresa.setSubdominio(empresa.getSubdominio().toLowerCase().trim());
        empresa.setActiva(false);

        Empresa empresaGuardada = empresaRepositorio.save(empresa);
        auditLogger.logCrear("Empresa", "Subdominio: " + empresaGuardada.getSubdominio());
        return empresaMapper.toResponse(empresaGuardada);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMINDEV')")
    public EmpresaDTO actualizar(Long id, EmpresaDTO dto) {
        Empresa empresa = empresaRepositorio.findById(id)
                .orElseThrow(() -> new BusinessException("Empresa no encontrada"));

        empresa.setNombreEmpresa(dto.getNombreEmpresa());
        empresa.setEmail(dto.getEmail());
        empresa.setTelefono(dto.getTelefono());
        empresa.setPlan(dto.getPlan());
        empresa.setActiva(dto.getActiva());

        Empresa empresaActualizada = empresaRepositorio.save(empresa);
        auditLogger.logActualizar("Empresa", String.valueOf(id));
        return empresaMapper.toResponse(empresaActualizada);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMINDEV')")
    public EmpresaDTO eliminar(Long id) {
        Empresa empresa = empresaRepositorio.findById(id)
                .orElseThrow(() -> new BusinessException("Empresa no encontrada"));
        empresa.setActiva(false);
        empresaRepositorio.save(empresa);
        auditLogger.logEliminar("Empresa", String.valueOf(id));
        return empresaMapper.toResponse(empresa);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMINDEV')")
    public EmpresaDTO buscarPorSubdominio(String subdominio) {
        EmpresaDTO empresa = empresaRepositorio.findBySubdominio(subdominio.toLowerCase())
                .map(empresaMapper::toResponse)
                .orElseThrow(() -> new BusinessException("Subdominio no encontrado"));
        auditLogger.logBuscar("Empresa", "Subdominio: " + subdominio);
        return empresa;
    }

    @PreAuthorize("hasRole('ADMINDEV')")
    public boolean existeNombreEmpresa(String nombreEmpresa) {
        return empresaRepositorio.findByNombreEmpresa(nombreEmpresa).isPresent();
    }

    @PreAuthorize("hasRole('ADMINDEV')")
    public boolean existeSubdominio(String subdominio) {
        return empresaRepositorio.findBySubdominio(subdominio.toLowerCase()).isPresent();
    }

    @Transactional
    @PreAuthorize("hasRole('ADMINDEV')")
    public EmpresaDTO actualizarEstadoActivo(Long id, Boolean activa) {
        Empresa empresa = empresaRepositorio.findById(id)
                .orElseThrow(() -> new BusinessException("Empresa no encontrada"));
        empresa.setActiva(activa);
        Empresa empresaActualizada = empresaRepositorio.save(empresa);

        if (activa) {
            auditLogger.logActivar("Empresa", String.valueOf(id));
        } else {
            auditLogger.logDesactivar("Empresa", String.valueOf(id));
        }

        return empresaMapper.toResponse(empresaActualizada);
    }
}