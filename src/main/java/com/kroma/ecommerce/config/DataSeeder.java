package com.kroma.ecommerce.config;

import com.kroma.ecommerce.model.*;
import com.kroma.ecommerce.model.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Inicializador de datos en MongoDB (10 productos skin-first, Admin, Cliente y Pedido demo).
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepo;
    private final ProductoRepository productoRepo;
    private final PedidoRepository pedidoRepo;

    @Value("${kroma.admin.email:admin@kroma.com.co}")
    private String adminEmail;

    @Value("${kroma.admin.password:KromaAdmin2026!}")
    private String adminPassword;

    public DataSeeder(UsuarioRepository usuarioRepo,
                      ProductoRepository productoRepo,
                      PedidoRepository pedidoRepo) {
        this.usuarioRepo = usuarioRepo;
        this.productoRepo = productoRepo;
        this.pedidoRepo = pedidoRepo;
    }

    @Override
    public void run(String... args) {
        seedAll();
    }

    public void seedAll() {
        if (usuarioRepo.findByCorreoIgnoreCase(adminEmail).isEmpty()) {
            Administrador admin = new Administrador(
                    "Valentina Rojas (Admin Kroma)",
                    adminEmail,
                    "ADM-KRM-001",
                    "Marketing, Operaciones y Garantías"
            );
            admin.establecerPassword(adminPassword);
            admin.validar();
            usuarioRepo.save(admin);
        }

        Cliente clienteDemo = (Cliente) usuarioRepo.findByCorreoIgnoreCase("camila@ejemplo.com.co")
                .orElseGet(() -> {
                    Cliente c = new Cliente(
                            "Camila Gómez",
                            "camila@ejemplo.com.co",
                            "1018456789",
                            "3104567890",
                            "Cra 15 # 85-24 Apto 402",
                            "Bogotá",
                            "Cundinamarca"
                    );
                    c.establecerPassword("Cliente123!");
                    c.setPreferenciaTono(new Cliente.PreferenciaTono("Neutro", "Mixta", "02 Arena Nude"));
                    c.validar();
                    return usuarioRepo.save(c);
                });

        if (productoRepo.count() == 0) {
            List<Producto> catalogo = List.of(
                    crearProd("KRM-001", "Base Sérum Skin-First Luminous",
                            "Cobertura modulable con ácido hialurónico y niacinamida que respira como tu segunda piel.",
                            "Ácido Hialurónico 2%, Niacinamida, Escualano vegetal, Vitamina E, Pigmentos minerales no abrasivos. Certificación Cruelty-Free.",
                            "Rostro", "Skin-First Nude", 58900, 69900, 42,
                            List.of("Seca", "Mixta", "Sensible"), "Neutro",
                            List.of(
                                    new Producto.TonoSwatch("01 Porcelana Rosa", "#F3D4C8", "Frío"),
                                    new Producto.TonoSwatch("02 Arena Nude", "#E3BE9F", "Neutro"),
                                    new Producto.TonoSwatch("03 Miel Dorada", "#C9986B", "Cálido"),
                                    new Producto.TonoSwatch("04 Canela Cálida", "#9E6B47", "Cálido")
                            ), "/images/products/base-serum.svg", true),

                    crearProd("KRM-002", "Corrector Hidratante Segunda Piel",
                            "Ilumina ojeras sin marcar líneas de expresión. Textura cremosa ligera de acabado natural.",
                            "Centella Asiática, Cafeína botánica, Manteca de Karité orgánica, Pantenol B5. Fórmula libre de abrasivos y Cruelty-Free.",
                            "Rostro", "Skin-First Nude", 36900, 42900, 55,
                            List.of("Todo tipo"), "Frío",
                            List.of(
                                    new Producto.TonoSwatch("10 Vainilla Fría", "#F5DFD6", "Frío"),
                                    new Producto.TonoSwatch("20 Durazno Neutro", "#E7BC9E", "Neutro"),
                                    new Producto.TonoSwatch("30 Caramelo Suave", "#BA855E", "Cálido")
                            ), "/images/products/corrector.svg", true),

                    crearProd("KRM-003", "Rubor Líquido Pétalo Blush",
                            "Efecto mejillas sonrojadas saludables con pigmentos botánicos de larga duración.",
                            "Aceite de Jojoba, Extracto de Rosa Mosqueta, Vitamina E, Mica ética no abrasiva. 100% Cruelty-Free.",
                            "Rostro", "Bloom Trendy", 34900, 0, 64,
                            List.of("Seca", "Mixta", "Grasa", "Sensible"), "Frío",
                            List.of(
                                    new Producto.TonoSwatch("Rosa Pétalo", "#E87A90", "Frío"),
                                    new Producto.TonoSwatch("Durazno Coral", "#EE8B6E", "Cálido"),
                                    new Producto.TonoSwatch("Malva Nude", "#B86B77", "Neutro")
                            ), "/images/products/rubor-liquido.svg", true),

                    crearProd("KRM-004", "Lip Oil Glossy Berry & Miel",
                            "Aceite labial nutritivo efecto espejo sin sensación pegajosa.",
                            "Aceite de Frambuesa, Escualano vegetal, Bisabolol calmante, Vitamina E natural. Vegano, no abrasivo y Cruelty-Free.",
                            "Labios", "Gloss & Care", 29900, 35000, 80,
                            List.of("Todo tipo"), "Universal",
                            List.of(
                                    new Producto.TonoSwatch("Rosa Cristal", "#F4A6B8", "Frío"),
                                    new Producto.TonoSwatch("Berry Dulce", "#B84A62", "Neutro"),
                                    new Producto.TonoSwatch("Néctar Miel", "#D98E6C", "Cálido")
                            ), "/images/products/lip-oil.svg", true),

                    crearProd("KRM-005", "Tinta Velvet Labios & Mejillas",
                            "Doble uso aterciopelado con color modulable ideal para un look natural colombiano.",
                            "Extracto de Hibisco, Glicerina vegetal, Manteca de Cacao colombiana, Tocoferol. Libre de abrasivos y Cruelty-Free.",
                            "Labios", "Skin-First Nude", 31900, 0, 48,
                            List.of("Todo tipo"), "Cálido",
                            List.of(
                                    new Producto.TonoSwatch("Terracota Nude", "#C26A5A", "Cálido"),
                                    new Producto.TonoSwatch("Frambuesa Soft", "#C74B6B", "Frío"),
                                    new Producto.TonoSwatch("Nude Chai", "#B57B69", "Neutro")
                            ), "/images/products/tinta-velvet.svg", false),

                    crearProd("KRM-006", "Polvo Suelto Translúcido Micro-Silk",
                            "Controla el brillo sin resecar ni tapar poros. Acabado filtro suave libre de talco.",
                            "Almidón de Arroz micronizado, Extracto de Té Verde, Sílice botánica no abrasiva, Ceramidas. Cruelty-Free certificado.",
                            "Rostro", "Skin-First Nude", 44900, 52000, 36,
                            List.of("Grasa", "Mixta"), "Neutro",
                            List.of(
                                    new Producto.TonoSwatch("Translúcido Puro", "#F7EFEA", "Universal"),
                                    new Producto.TonoSwatch("Banana Suave", "#F1DFBA", "Cálido"),
                                    new Producto.TonoSwatch("Rosa Iluminador", "#F6DDE2", "Frío")
                            ), "/images/products/polvo-silk.svg", false),

                    crearProd("KRM-007", "Gel Fijador de Cejas Laminado Natural",
                            "Fija y nutre tus cejas todo el día sin dejar residuos blancos ni endurecer.",
                            "Biotina vegetal, Pantenol, Aceite de Ricino orgánico, Polímero botánico flexible. Cruelty-Free y libre de abrasivos.",
                            "Cejas", "Brow Sculpt", 27900, 0, 52,
                            List.of("Todo tipo"), "Universal",
                            List.of(
                                    new Producto.TonoSwatch("Transparente Glow", "#E8E4E6", "Universal"),
                                    new Producto.TonoSwatch("Café Suave", "#7A5C4B", "Neutro"),
                                    new Producto.TonoSwatch("Espresso Oscuro", "#44322B", "Frío")
                            ), "/images/products/gel-cejas.svg", false),

                    crearProd("KRM-008", "Pestañina Lash-Care Volumen Botánico",
                            "Alarga y define pestañas con ceras vegetales fáciles de retirar sin maltratar.",
                            "Cera de Candelilla, Aceite de Almendras dulces, Vitamina E, Pigmento carbón vegetal no abrasivo. Cruelty-Free.",
                            "Ojos", "Clean Eyes", 33900, 39900, 60,
                            List.of("Sensible", "Todo tipo"), "Universal",
                            List.of(
                                    new Producto.TonoSwatch("Negro Intenso", "#1F1B1D", "Universal"),
                                    new Producto.TonoSwatch("Café Natural", "#5D4037", "Cálido")
                            ), "/images/products/pestanina.svg", true),

                    crearProd("KRM-009", "Paleta Sombras Nude Colombiana (6 Tonos)",
                            "Tonos neutros, rosados y tierra aterciopelados de alta difuminación diaria.",
                            "Manteca de Murumuru, Escualano vegetal, Pigmentos minerales suaves libres de talco y abrasivos. Cruelty-Free.",
                            "Ojos", "Skin-First Nude", 54900, 64900, 28,
                            List.of("Todo tipo"), "Cálido",
                            List.of(
                                    new Producto.TonoSwatch("Atardecer Cafetero", "#C68B6B", "Cálido"),
                                    new Producto.TonoSwatch("Rosa Andino", "#D89AA7", "Frío"),
                                    new Producto.TonoSwatch("Arena Neutra", "#D2B49C", "Neutro")
                            ), "/images/products/paleta-sombras.svg", false),

                    crearProd("KRM-010", "Bruma Facial Preparadora Calma & Glow",
                            "Prepara e hidrata la barrera cutánea antes del maquillaje con agua de rosas.",
                            "Agua de Rosas Damascena, Ácido Hialurónico, Niacinamida 3%, Aloe Vera orgánico, Alantoína. Cruelty-Free y cero abrasivos.",
                            "Skincare", "Skin-First Prep", 39900, 0, 44,
                            List.of("Seca", "Sensible", "Mixta"), "Universal",
                            List.of(new Producto.TonoSwatch("Fórmula Pure Rose", "#F9D6DF", "Universal")),
                            "/images/products/bruma-facial.svg", false)
            );
            catalogo.forEach(Producto::validar);
            productoRepo.saveAll(catalogo);
        }

        if (pedidoRepo.count() == 0) {
            List<Producto> prods = productoRepo.findAll();
            if (prods.size() >= 2) {
                Producto p1 = prods.get(0);
                Producto p2 = prods.get(3 < prods.size() ? 3 : 1);
                Pedido demo = new Pedido();
                demo.setNumeroPedido("KRM-104820");
                demo.setClienteId(clienteDemo.getId());
                demo.setDatosCliente(new Pedido.DatosCliente(
                        clienteDemo.getNombre(), clienteDemo.getCedula(),
                        clienteDemo.getCorreo(), clienteDemo.getTelefono()));
                demo.setDireccionEntrega(new Pedido.DireccionEntrega(
                        clienteDemo.getDireccion(), clienteDemo.getCiudad(),
                        clienteDemo.getDepartamento(), "Portería edificio"));
                demo.setItems(List.of(
                        new DetallePedido(p1.getId(), p1.getCodigo(), p1.getNombre(), "02 Arena Nude", "#E3BE9F", 1, p1.getPrecio()),
                        new DetallePedido(p2.getId(), p2.getCodigo(), p2.getNombre(), "Berry Dulce", "#B84A62", 1, p2.getPrecio())
                ));
                demo.setMetodoPago("Nequi");
                demo.setTipoEmpaqueSostenible("Caja Biodegradable Bagazo de Caña");
                demo.setAceptoLey1480(true);
                demo.setEstadoPedido("Entregado");
                demo.setFechaPedido(Instant.now().minus(Duration.ofDays(2)));
                demo.setFechaEntregaReal(Instant.now().minus(Duration.ofDays(1)));
                demo.validar();
                pedidoRepo.save(demo);
            }
        }
    }

    private Producto crearProd(String codigo, String nombre, String descCorta, String ingredientes,
                               String categoria, String linea, long precio, long precioAnterior, int stock,
                               List<String> tipoPiel, String gamaTono, List<Producto.TonoSwatch> paleta,
                               String imagenUrl, boolean destacado) {
        Producto p = new Producto();
        p.setCodigo(codigo);
        p.setNombre(nombre);
        p.setDescripcionCorta(descCorta);
        p.setDescripcionIngredientes(ingredientes);
        p.setCertificacionCrueltyFree(true);
        p.setLibreDeAbrasivos(true);
        p.setCategoria(categoria);
        p.setLinea(linea);
        p.setPrecio(precio);
        p.setPrecioAnterior(precioAnterior);
        p.setStock(stock);
        p.setTipoPiel(tipoPiel);
        p.setGamaTono(gamaTono);
        p.setPaletaTonos(paleta);
        p.setImagenUrl(imagenUrl);
        p.setDestacado(destacado);
        p.setActivo(true);
        return p;
    }
}
