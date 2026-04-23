package com.mvprestaurante.mvp.config;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.mvprestaurante.mvp.enums.TipoItem;
import com.mvprestaurante.mvp.enums.UnidadMedida;
import com.mvprestaurante.mvp.models.DetalleReceta;
import com.mvprestaurante.mvp.models.Empresa;
import com.mvprestaurante.mvp.models.Ingrediente;
import com.mvprestaurante.mvp.models.Producto;
import com.mvprestaurante.mvp.models.Receta;
import com.mvprestaurante.mvp.models.Usuario;
import com.mvprestaurante.mvp.repositories.DetalleRecetaRepository;
import com.mvprestaurante.mvp.repositories.EmpresaRepositorio;
import com.mvprestaurante.mvp.repositories.IngredienteRepository;
import com.mvprestaurante.mvp.repositories.ProductoRepository;
import com.mvprestaurante.mvp.repositories.RecetaRepository;
import com.mvprestaurante.mvp.repositories.UsuarioRepositorio;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class SuperAdminInitializer implements CommandLineRunner {

    private final UsuarioRepositorio usuarioRepositorio;
    private final EmpresaRepositorio empresaRepositorio;
    private final IngredienteRepository ingredienteRepository;
    private final ProductoRepository productoRepository;
    private final RecetaRepository recetaRepository;
    private final DetalleRecetaRepository detalleRecetaRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (!usuarioRepositorio.existsByRol("ADMINDEV")) {
            Usuario superadmin = Usuario.builder()
                    .nombre("Super Admin")
                    .nombreUsuario("joseKLMora")
                    .contrasenna(passwordEncoder.encode("superAdmin123456"))
                    .rol("ADMINDEV")
                    .esSuperadmin(true)
                    .estaActivo(true)
                    .build();

            usuarioRepositorio.save(superadmin);
            log.info("Superadmin creado: joseKLMora con rol ADMINDEV");
        }

        if (empresaRepositorio.count() == 0) {
            Empresa demo = Empresa.builder()
                    .subdominio("restamodelo")
                    .nombreEmpresa("Empresa Modelo")
                    .email("demo@restamodelo.com")
                    .telefono("3001234567")
                    .plan("PREMIUM")
                    .activa(true)
                    .build();

            demo = empresaRepositorio.save(demo);
            log.info("Empresa demo creada: restamodelo");

            Usuario adminDemo = Usuario.builder()
                    .nombre("Administrador Demo")
                    .nombreUsuario("admin")
                    .contrasenna(passwordEncoder.encode("admin123"))
                    .rol("ADMIN")
                    .esSuperadmin(false)
                    .estaActivo(true)
                    .empresa(demo)
                    .build();

            usuarioRepositorio.save(adminDemo);
            log.info("Usuario demo creado: admin");

            crearDatosDemo(demo);
        }
    }

    private void crearDatosDemo(Empresa empresa) {
        List<Ingrediente> ingredientes = new ArrayList<>();

        String[] nombresIng = {
            "Pan de hamburguesa", "Carne molida", "Lechuga", "Tomate", "Queso", "Salsa",
            "Pan para perro", "Salchicha", "Mostaza", "Ketchup", "Cebolla",
            "Papa", "Aceite", "Sal", "Base de pizza", "Salsa de tomate",
            "Jamón", "Pan tajado", "Mantequilla", "Tortillas", "Carne",
            "Cilantro", "Harina de maíz", "Agua", "Pollo", "Harina",
            "Huevo", "Pan rallado", "Masa", "Salsas", "Arroz",
            "Frijoles", "Aguacate", "Limón", "Oregano"
        };

        String[] unidades = {
            "UNIDAD", "KG", "KG", "KG", "KG", "ML",
            "UNIDAD", "UNIDAD", "ML", "ML", "KG",
            "KG", "ML", "G", "UNIDAD", "ML",
            "KG", "UNIDAD", "G", "UNIDAD", "KG",
            "KG", "KG", "KG", "ML", "KG", "KG",
            "UNIDAD", "G", "KG", "ML", "KG",
            "KG", "UNIDAD", "UNIDAD", "G"
        };

        Map<String, Ingrediente> mapaIngredientes = new HashMap<>();
        for (int i = 0; i < nombresIng.length; i++) {
            Ingrediente ing = Ingrediente.builder()
                    .nombre(nombresIng[i])
                    .stockDisponible(50.0)
                    .precioCompra(BigDecimal.valueOf(1000.0))
                    .unidadMedida(UnidadMedida.valueOf(unidades[i].toUpperCase()))
                    .estaActivo(true)
                    .empresa(empresa)
                    .build();
            ing = ingredienteRepository.save(ing);
            ingredientes.add(ing);
            mapaIngredientes.put(nombresIng[i], ing);
        }
        log.info("Ingredientes demo creados: " + ingredientes.size());

        List<Producto> productos = new ArrayList<>();
        
        Object[][] productosData = {
            {"Hamburguesa clásica", "Deliciosa hamburguesa con todos los toppings", 12000.0, false},
            {"Hamburguesa con queso", "Hamburguesa con doble queso cheddar", 14000.0, false},
            {"Perro caliente", "Pan con salchichia y salsas", 8000.0, false},
            {"Perro especial", "Perro con jamón y queso", 10000.0, false},
            {"Papas fritas", "Papas crujientes golden", 6000.0, false},
            {"Papas con queso", "Papas con queso fundido", 8000.0, false},
            {"Pizza media", "Pizza mediana con jamón y queso", 15000.0, false},
            {"Pizza completa", "Pizza grande con múltiples toppings", 25000.0, false},
            {"Sandwich mixto", "Sandwich toasted con jamón y queso", 7000.0, false},
            {"Tacos (3 und)", "Tacos mexicanos con carne", 10000.0, false},
            {"Arepa con queso", "Arepa rellena de queso", 6000.0, false},
            {"Nuggets (8 und)", "Nuggets de pollo golden", 9000.0, false},
            {"Salchipapas", "Papas con salchicha y salsas", 10000.0, false},
            {"Empanadas (2 und)", "Empanadas de carne", 7000.0, false},
            {"Coca Cola", "Bebida gasificada 400ml", 2500.0, false},
            {"Pepsi", "Bebida gasificada 400ml", 2500.0, false},
            {"Sprite", "Bebida gasificada 400ml", 2500.0, false},
            {"Agua mineral", "Agua sin gas 600ml", 2000.0, false},
            {"Jugo Natural", "Jugo de fruta natural 300ml", 3000.0, false},
            {"Cerveza", "Cerveza nacional 330ml", 4000.0, false}
        };

        for (Object[] data : productosData) {
            Producto prod = Producto.builder()
                    .nombre((String) data[0])
                    .descripcion((String) data[1])
                    .precioVenta(BigDecimal.valueOf((Double) data[2]))
                    .precioCompra(BigDecimal.valueOf((Double) data[2] * 0.6).setScale(2, RoundingMode.HALF_UP))
                    .tieneReceta((Boolean) data[3])
                    .estaActivo(true)
                    .stock(100.0)
                    .empresa(empresa)
                    .build();
            productos.add(productoRepository.save(prod));
        }
        log.info("Productos demo creados: " + productos.size());

        String[][] recetasData = {
            {"Hamburguesa clásica", "Pan de hamburguesa,Carne molida,Lechuga,Tomate,Queso,Salsa", "1,0.15,0.05,0.08,0.04,0.02"},
            {"Perro caliente", "Pan para perro,Salchicha,Mostaza,Ketchup,Cebolla", "1,1,0.015,0.015,0.02"},
            {"Papas fritas", "Papa,Aceite,Sal", "0.3,0.5,0.005"},
            {"Pizza media", "Base de pizza,Salsa de tomate,Queso,Jamón", "1,0.1,0.15,0.08"},
            {"Sandwich mixto", "Pan tajado,Jamón,Queso,Mantequilla", "2,0.05,0.04,0.01"},
            {"Tacos (3 und)", "Tortillas,Carne,Cebolla,Cilantro,Salsas", "3,0.15,0.03,0.02,0.03"},
            {"Arepa con queso", "Harina de maíz,Agua,Queso", "0.1,0.12,0.05"},
            {"Nuggets (8 und)", "Pollo,Harina,Huevo,Pan rallado,Aceite", "0.2,0.05,0.02,0.08,0.5"},
            {"Salchipapas", "Papa,Salchicha,Aceite,Salsas", "0.3,2,0.5,0.03"},
            {"Empanadas", "Masa,Carne,Cebolla,Aceite", "0.2,0.15,0.02,0.3"}
        };

        for (String[] recetaData : recetasData) {
            String nombreReceta = recetaData[0];
            String[] ingNames = recetaData[1].split(",");
            String[] cantidadesStr = recetaData[2].split(",");
            
            for (Producto prod : productos) {
                if (prod.getNombre().equals(nombreReceta)) {
                    Receta receta = Receta.builder()
                            .nombre("Receta de " + nombreReceta)
                            .descripcion("Preparación de " + nombreReceta)
                            .precioBruto(BigDecimal.valueOf(5000.0))
                            .precioVenta(prod.getPrecioVenta())
                            .estaActiva(true)
                            .producto(prod)
                            .empresa(empresa)
                            .build();
                    
                    receta = recetaRepository.save(receta);
                    
                    for (int j = 0; j < ingNames.length; j++) {
                        String nombreIng = ingNames[j];
                        Double cantidad = Double.parseDouble(cantidadesStr[j]);
                        
                        Ingrediente ing = mapaIngredientes.get(nombreIng);
                        if (ing != null) {
                            DetalleReceta detalle = DetalleReceta.builder()
                                    .receta(receta)
                                    .ingrediente(ing)
                                    .cantidadIngrediente(cantidad)
                                    .nombre(ing.getNombre())
                                    .build();
                            detalleRecetaRepository.save(detalle);
                        }
                    }
                    
                    prod.setReceta(receta);
                    prod.setTieneReceta(true);
                    productoRepository.save(prod);
                    
                    break;
                }
            }
        }
        log.info("Recetas y DetalleReceta creados");
    }
}
