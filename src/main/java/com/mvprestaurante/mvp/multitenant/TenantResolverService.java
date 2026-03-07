package com.mvprestaurante.mvp.multitenant;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.mvprestaurante.mvp.models.Empresa;
import com.mvprestaurante.mvp.repositories.EmpresaRepositorio;

@Service
public class TenantResolverService {

    // esta clase se encarga de resolver el tenantId a partir del subdominio
    // aqui va la logica de negocio para validar el subdominio, buscar la empresa y
    // verificar su estado

    private final EmpresaRepositorio empresaRepositorio;

    public TenantResolverService(EmpresaRepositorio empresaRepositorio) {
        this.empresaRepositorio = empresaRepositorio;
    }

    public String resolveTenantId(String subdominio) {

        if (subdominio == null || subdominio.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Subdominio inválido");
        }

        Empresa empresa = empresaRepositorio
                .findBySubdominio(subdominio)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Empresa no encontrada"));

        if (!Boolean.TRUE.equals(empresa.getActiva())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Empresa inactiva");
        }

        return empresa.getId().toString();
    }

}