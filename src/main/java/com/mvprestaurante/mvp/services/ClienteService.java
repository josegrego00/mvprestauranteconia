package com.mvprestaurante.mvp.services;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.mvprestaurante.mvp.models.Cliente;
import com.mvprestaurante.mvp.models.Empresa;
import com.mvprestaurante.mvp.models.Usuario;
import com.mvprestaurante.mvp.repositories.ClienteRepositorio;
import com.mvprestaurante.mvp.repositories.UsuarioRepositorio;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepositorio clienteRepositorio;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void crearClientePorDefecto() {
        Cliente cliente = new Cliente();
        cliente.setNombre("Consumidor Final");
        cliente.setEmail("cunsumidorfinal@gmail.com");
        cliente.setTelefono("555-55555");
        cliente.setEstaActivo(true);
        cliente.setFechaRegistro(LocalDateTime.now());
        // Puedes generar un documento genérico o dejarlo null
        cliente.setDocumentoIdentidad("99999999");

        Cliente clienteGuardado = clienteRepositorio.save(cliente);
    }
}