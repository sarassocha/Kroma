package com.kroma.ecommerce.controller;

import com.kroma.ecommerce.model.Cliente;
import com.kroma.ecommerce.model.Producto;
import com.kroma.ecommerce.model.ToneMatcherService;
import com.kroma.ecommerce.model.Usuario;
import com.kroma.ecommerce.model.repository.ProductoRepository;
import com.kroma.ecommerce.model.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controlador CatalogoController (Hereda de BaseController).
 * Gestiona la vista principal de tienda estilo Trendy, filtrado dinámico (RF02)
 * y el asistente virtual Tone Matcher (RF03).
 */
@Controller
public class CatalogoController extends BaseController {

    private final ProductoRepository productoRepo;
    private final UsuarioRepository usuarioRepo;
    private final ToneMatcherService toneMatcherService;

    public CatalogoController(ProductoRepository productoRepo,
                              UsuarioRepository usuarioRepo,
                              ToneMatcherService toneMatcherService) {
        this.productoRepo = productoRepo;
        this.usuarioRepo = usuarioRepo;
        this.toneMatcherService = toneMatcherService;
    }

    @GetMapping("/")
    public String mostrarTienda(@RequestParam(defaultValue = "Todos") String categoria,
                                @RequestParam(defaultValue = "Todos") String tipoPiel,
                                @RequestParam(defaultValue = "Todos") String gamaTono,
                                @RequestParam(defaultValue = "") String q,
                                Model model) {
        List<Producto> productos = filtrarProductos(categoria, tipoPiel, gamaTono, q);

        model.addAttribute("titulo", "Kroma Cosmetics S.A.S. | Maquillaje Skin-First & Cruelty-Free");
        model.addAttribute("productos", productos);
        model.addAttribute("categorias", List.of("Todos", "Rostro", "Labios", "Ojos", "Cejas", "Skincare"));
        model.addAttribute("tiposPiel", List.of("Todos", "Seca", "Mixta", "Grasa", "Sensible"));
        model.addAttribute("gamasTono", List.of("Todos", "Frío", "Neutro", "Cálido", "Universal"));
        model.addAttribute("filtroCategoria", categoria);
        model.addAttribute("filtroTipoPiel", tipoPiel);
        model.addAttribute("filtroGamaTono", gamaTono);
        model.addAttribute("filtroQ", q);
        return "client/index";
    }

    @GetMapping("/api/productos")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> listarProductosApi(
            @RequestParam(defaultValue = "Todos") String categoria,
            @RequestParam(defaultValue = "Todos") String tipoPiel,
            @RequestParam(defaultValue = "Todos") String gamaTono,
            @RequestParam(defaultValue = "") String q) {
        List<Producto> productos = filtrarProductos(categoria, tipoPiel, gamaTono, q);
        return ok(Map.of("total", productos.size(), "productos", productos));
    }

    @GetMapping("/api/productos/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obtenerDetalleProducto(@PathVariable String id) {
        Optional<Producto> prod = productoRepo.findById(id);
        return prod.map(producto -> ok(Map.of("producto", producto)))
                .orElseGet(() -> fail("Producto no encontrado.", 404));
    }

    @GetMapping("/tone-matcher")
    public String mostrarToneMatcher(Model model) {
        model.addAttribute("titulo", "Tone Matcher Interactivo | Kroma Cosmetics");
        return "client/tone-matcher";
    }

    @PostMapping("/api/tone-matcher")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> procesarToneMatcher(@RequestBody Map<String, String> body,
                                                                   HttpSession session) {
        String colorVenas = body.getOrDefault("colorVenas", "azuladas");
        String reaccionSol = body.getOrDefault("reaccionSol", "se_quema");
        String joyeria = body.getOrDefault("joyeria", "plateada");
        String tipoPiel = body.getOrDefault("tipoPiel", "Mixta");

        ToneMatcherService.Diagnostico diagnostico =
                toneMatcherService.diagnosticarSubtono(colorVenas, reaccionSol, joyeria);
        List<Producto> activos = productoRepo.findByActivoTrueOrderByDestacadoDescFechaCreacionDesc();
        List<ToneMatcherService.Recomendacion> recomendaciones =
                toneMatcherService.recomendarProductos(activos, diagnostico.subtono(), tipoPiel);

        Map<String, Object> actual = usuarioActual(session);
        if (actual != null && "cliente".equals(actual.get("rol"))) {
            String id = String.valueOf(actual.get("id"));
            usuarioRepo.findById(id).ifPresent(u -> {
                if (u instanceof Cliente cliente) {
                    String tonoSug = recomendaciones.isEmpty() ? "" : recomendaciones.get(0).getTonoRecomendado().getNombreTono();
                    cliente.setPreferenciaTono(new Cliente.PreferenciaTono(diagnostico.subtono(), tipoPiel, tonoSug));
                    usuarioRepo.save(cliente);
                }
            });
        }

        return ok(Map.of(
                "diagnostico", diagnostico,
                "tipoPiel", tipoPiel,
                "recomendaciones", recomendaciones
        ));
    }

    private List<Producto> filtrarProductos(String categoria, String tipoPiel, String gamaTono, String q) {
        String queryLower = q != null ? q.trim().toLowerCase() : "";
        return productoRepo.findByActivoTrueOrderByDestacadoDescFechaCreacionDesc().stream()
                .filter(p -> "Todos".equalsIgnoreCase(categoria) || categoria.equalsIgnoreCase(p.getCategoria()))
                .filter(p -> "Todos".equalsIgnoreCase(tipoPiel)
                        || p.getTipoPiel().contains(tipoPiel)
                        || p.getTipoPiel().contains("Todo tipo"))
                .filter(p -> "Todos".equalsIgnoreCase(gamaTono) || gamaTono.equalsIgnoreCase(p.getGamaTono()))
                .filter(p -> queryLower.isEmpty()
                        || p.getNombre().toLowerCase().contains(queryLower)
                        || p.getDescripcionCorta().toLowerCase().contains(queryLower)
                        || p.getDescripcionIngredientes().toLowerCase().contains(queryLower)
                        || p.getCategoria().toLowerCase().contains(queryLower))
                .toList();
    }
}
