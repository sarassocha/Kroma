package com.kroma.ecommerce.controller;

import com.kroma.ecommerce.model.GarantiaTono;
import com.kroma.ecommerce.model.Pedido;
import com.kroma.ecommerce.model.Producto;
import com.kroma.ecommerce.model.repository.GarantiaTonoRepository;
import com.kroma.ecommerce.model.repository.PedidoRepository;
import com.kroma.ecommerce.model.repository.ProductoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

/**
 * Controlador AdminController (Hereda de BaseController).
 * Panel Administrativo exclusivo de Kroma (RU03):
 * - Agregar productos
 * - Editar especificaciones de producto
 * - Eliminar productos
 * - Ver historial de compras y gestionar garantías de tono
 */
@Controller
@RequestMapping("/admin")
public class AdminController extends BaseController {

    private final ProductoRepository productoRepo;
    private final PedidoRepository pedidoRepo;
    private final GarantiaTonoRepository garantiaRepo;

    public AdminController(ProductoRepository productoRepo,
                           PedidoRepository pedidoRepo,
                           GarantiaTonoRepository garantiaRepo) {
        this.productoRepo = productoRepo;
        this.pedidoRepo = pedidoRepo;
        this.garantiaRepo = garantiaRepo;
    }

    @GetMapping
    public String mostrarDashboard(@RequestParam(required = false) String ok,
                                   @RequestParam(required = false) String error,
                                   Model model) {
        List<Producto> productos = productoRepo.findAllByOrderByFechaCreacionDesc();
        List<Pedido> pedidos = pedidoRepo.findAllByOrderByFechaPedidoDesc();
        List<GarantiaTono> garantias = garantiaRepo.findAllByOrderByFechaSolicitudDesc();

        long ingresosTotales = pedidos.stream().mapToLong(Pedido::getTotal).sum();
        long stockBajo = productos.stream().filter(p -> p.getStock() <= 10).count();
        long garantiasPendientes = garantias.stream().filter(g -> "Pendiente".equals(g.getEstadoSolicitud())).count();

        model.addAttribute("titulo", "Panel Administrativo | Kroma Cosmetics S.A.S.");
        model.addAttribute("productos", productos);
        model.addAttribute("pedidos", pedidos);
        model.addAttribute("garantias", garantias);
        model.addAttribute("totalProductos", productos.size());
        model.addAttribute("totalPedidos", pedidos.size());
        model.addAttribute("ingresosTotales", ingresosTotales);
        model.addAttribute("stockBajo", stockBajo);
        model.addAttribute("garantiasPendientes", garantiasPendientes);
        model.addAttribute("flashOk", ok);
        model.addAttribute("flashError", error);
        return "admin/dashboard";
    }

    @PostMapping("/productos")
    public String agregarProductoWeb(@RequestParam String codigo,
                                     @RequestParam String nombre,
                                     @RequestParam String descripcionCorta,
                                     @RequestParam String descripcionIngredientes,
                                     @RequestParam String categoria,
                                     @RequestParam(defaultValue = "Skin-First Nude") String linea,
                                     @RequestParam long precio,
                                     @RequestParam(defaultValue = "0") long precioAnterior,
                                     @RequestParam int stock,
                                     @RequestParam(defaultValue = "Todo tipo") List<String> tipoPiel,
                                     @RequestParam(defaultValue = "Universal") String gamaTono,
                                     @RequestParam(defaultValue = "") String paletaTonos,
                                     @RequestParam(defaultValue = "/images/product-showcase.jpg") String imagenUrl,
                                     @RequestParam(defaultValue = "false") boolean destacado,
                                     @RequestParam(defaultValue = "false") boolean certificacionCrueltyFree) {
        try {
            Producto prod = new Producto();
            llenarProducto(prod, codigo, nombre, descripcionCorta, descripcionIngredientes,
                    categoria, linea, precio, precioAnterior, stock, tipoPiel, gamaTono,
                    paletaTonos, imagenUrl, destacado, certificacionCrueltyFree);
            prod.validar();
            productoRepo.save(prod);
            return "redirect:/admin?ok=" + encode("Producto '" + prod.getNombre() + "' agregado al catálogo.");
        } catch (Exception e) {
            return "redirect:/admin?error=" + encode(e.getMessage());
        }
    }

    @PostMapping("/productos/{id}/editar")
    public String editarProductoWeb(@PathVariable String id,
                                    @RequestParam String codigo,
                                    @RequestParam String nombre,
                                    @RequestParam String descripcionCorta,
                                    @RequestParam String descripcionIngredientes,
                                    @RequestParam String categoria,
                                    @RequestParam(defaultValue = "Skin-First Nude") String linea,
                                    @RequestParam long precio,
                                    @RequestParam(defaultValue = "0") long precioAnterior,
                                    @RequestParam int stock,
                                    @RequestParam(defaultValue = "Todo tipo") List<String> tipoPiel,
                                    @RequestParam(defaultValue = "Universal") String gamaTono,
                                    @RequestParam(defaultValue = "") String paletaTonos,
                                    @RequestParam(defaultValue = "/images/product-showcase.jpg") String imagenUrl,
                                    @RequestParam(defaultValue = "false") boolean destacado,
                                    @RequestParam(defaultValue = "false") boolean certificacionCrueltyFree) {
        try {
            Producto prod = productoRepo.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado."));
            llenarProducto(prod, codigo, nombre, descripcionCorta, descripcionIngredientes,
                    categoria, linea, precio, precioAnterior, stock, tipoPiel, gamaTono,
                    paletaTonos, imagenUrl, destacado, certificacionCrueltyFree);
            prod.validar();
            productoRepo.save(prod);
            return "redirect:/admin?ok=" + encode("Especificaciones de '" + prod.getNombre() + "' actualizadas.");
        } catch (Exception e) {
            return "redirect:/admin?error=" + encode(e.getMessage());
        }
    }

    @PostMapping("/productos/{id}/eliminar")
    public String eliminarProductoWeb(@PathVariable String id) {
        Optional<Producto> opt = productoRepo.findById(id);
        if (opt.isEmpty()) {
            return "redirect:/admin?error=" + encode("Producto no encontrado.");
        }
        String nombre = opt.get().getNombre();
        productoRepo.deleteById(id);
        return "redirect:/admin?ok=" + encode("Producto '" + nombre + "' eliminado del catálogo.");
    }

    // Endpoints API REST para pruebas e integración dinámica
    @GetMapping("/api/pedidos")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> listarHistorialComprasApi() {
        List<Pedido> pedidos = pedidoRepo.findAllByOrderByFechaPedidoDesc();
        return ok(Map.of("total", pedidos.size(), "pedidos", pedidos));
    }

    @PostMapping("/api/productos")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> agregarProductoApi(@RequestBody Map<String, Object> body) {
        try {
            Producto prod = new Producto();
            aplicarDesdeMapa(prod, body);
            prod.validar();
            Producto guardado = productoRepo.save(prod);
            return ResponseEntity.status(201).body(Map.of(
                    "ok", true,
                    "mensaje", "Producto agregado correctamente.",
                    "producto", guardado
            ));
        } catch (Exception e) {
            return fail(e.getMessage(), 400);
        }
    }

    @PutMapping("/api/productos/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> editarProductoApi(@PathVariable String id,
                                                                 @RequestBody Map<String, Object> body) {
        try {
            Producto prod = productoRepo.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado."));
            aplicarDesdeMapa(prod, body);
            prod.validar();
            Producto guardado = productoRepo.save(prod);
            return ok(Map.of("mensaje", "Especificaciones actualizadas.", "producto", guardado));
        } catch (Exception e) {
            return fail(e.getMessage(), 400);
        }
    }

    @DeleteMapping("/api/productos/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> eliminarProductoApi(@PathVariable String id) {
        Optional<Producto> opt = productoRepo.findById(id);
        if (opt.isEmpty()) return fail("Producto no encontrado.", 404);
        productoRepo.deleteById(id);
        return ok(Map.of("mensaje", "Producto '" + opt.get().getNombre() + "' eliminado."));
    }

    @PatchMapping("/api/pedidos/{id}/estado")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> actualizarEstadoPedido(@PathVariable String id,
                                                                      @RequestBody Map<String, String> body) {
        Optional<Pedido> opt = pedidoRepo.findById(id);
        if (opt.isEmpty()) return fail("Pedido no encontrado.", 404);
        Pedido pedido = opt.get();
        String nuevoEstado = body.get("estadoPedido");
        pedido.setEstadoPedido(nuevoEstado);
        if ("Entregado".equals(nuevoEstado)) {
            pedido.setFechaEntregaReal(Instant.now());
        }
        pedidoRepo.save(pedido);
        return ok(Map.of("pedido", pedido));
    }

    @PatchMapping("/api/garantias/{id}/estado")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> actualizarEstadoGarantia(@PathVariable String id,
                                                                        @RequestBody Map<String, String> body) {
        Optional<GarantiaTono> opt = garantiaRepo.findById(id);
        if (opt.isEmpty()) return fail("Solicitud no encontrada.", 404);
        GarantiaTono g = opt.get();
        g.setEstadoSolicitud(body.get("estadoSolicitud"));
        garantiaRepo.save(g);
        return ok(Map.of("garantia", g));
    }

    private void llenarProducto(Producto prod, String codigo, String nombre, String descCorta,
                                String ingredientes, String categoria, String linea, long precio,
                                long precioAnterior, int stock, List<String> tipoPiel, String gamaTono,
                                String paletaTonos, String imagenUrl, boolean destacado, boolean crueltyFree) {
        prod.setCodigo(codigo);
        prod.setNombre(nombre);
        prod.setDescripcionCorta(descCorta);
        prod.setDescripcionIngredientes(ingredientes);
        prod.setCategoria(categoria);
        prod.setLinea(linea);
        prod.setPrecio(precio);
        prod.setPrecioAnterior(precioAnterior);
        prod.setStock(stock);
        prod.setTipoPiel(tipoPiel);
        prod.setGamaTono(gamaTono);
        prod.setPaletaTonos(parsearPaleta(paletaTonos));
        prod.setImagenUrl(imagenUrl);
        prod.setDestacado(destacado);
        prod.setCertificacionCrueltyFree(crueltyFree);
        prod.setLibreDeAbrasivos(true);
    }

    @SuppressWarnings("unchecked")
    private void aplicarDesdeMapa(Producto prod, Map<String, Object> body) {
        if (body.containsKey("codigo")) prod.setCodigo(String.valueOf(body.get("codigo")));
        if (body.containsKey("nombre")) prod.setNombre(String.valueOf(body.get("nombre")));
        if (body.containsKey("descripcionCorta")) prod.setDescripcionCorta(String.valueOf(body.get("descripcionCorta")));
        if (body.containsKey("descripcionIngredientes")) prod.setDescripcionIngredientes(String.valueOf(body.get("descripcionIngredientes")));
        if (body.containsKey("categoria")) prod.setCategoria(String.valueOf(body.get("categoria")));
        if (body.containsKey("linea")) prod.setLinea(String.valueOf(body.get("linea")));
        if (body.containsKey("precio")) prod.setPrecio(Long.parseLong(String.valueOf(body.get("precio"))));
        if (body.containsKey("precioAnterior")) prod.setPrecioAnterior(Long.parseLong(String.valueOf(body.get("precioAnterior"))));
        if (body.containsKey("stock")) prod.setStock(Integer.parseInt(String.valueOf(body.get("stock"))));
        if (body.containsKey("gamaTono")) prod.setGamaTono(String.valueOf(body.get("gamaTono")));
        if (body.containsKey("imagenUrl")) prod.setImagenUrl(String.valueOf(body.get("imagenUrl")));
        if (body.containsKey("destacado")) prod.setDestacado(Boolean.parseBoolean(String.valueOf(body.get("destacado"))));
        if (body.containsKey("certificacionCrueltyFree")) prod.setCertificacionCrueltyFree(Boolean.parseBoolean(String.valueOf(body.get("certificacionCrueltyFree"))));
        if (body.containsKey("libreDeAbrasivos")) prod.setLibreDeAbrasivos(Boolean.parseBoolean(String.valueOf(body.get("libreDeAbrasivos"))));
        if (body.containsKey("tipoPiel") && body.get("tipoPiel") instanceof List<?> lista) {
            prod.setTipoPiel(lista.stream().map(String::valueOf).toList());
        }
        if (body.containsKey("paletaTonos")) {
            prod.setPaletaTonos(parsearPaleta(String.valueOf(body.get("paletaTonos"))));
        }
    }

    private List<Producto.TonoSwatch> parsearPaleta(String raw) {
        if (raw == null || raw.isBlank()) return new ArrayList<>();
        List<Producto.TonoSwatch> lista = new ArrayList<>();
        for (String bloque : raw.split(",")) {
            String trimmed = bloque.trim();
            if (trimmed.isEmpty()) continue;
            String[] partes = trimmed.split("\\|");
            String nombreTono = partes[0].trim();
            String hex = partes.length > 1 && partes[1].trim().matches("^#[0-9A-Fa-f]{6}$") ? partes[1].trim() : "#E8B4A0";
            String subtono = partes.length > 2 ? partes[2].trim() : "Neutro";
            lista.add(new Producto.TonoSwatch(nombreTono, hex, subtono));
        }
        return lista;
    }

    private String encode(String text) {
        return URLEncoder.encode(text, StandardCharsets.UTF_8);
    }
}
