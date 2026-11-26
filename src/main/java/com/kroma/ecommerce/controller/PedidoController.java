package com.kroma.ecommerce.controller;

import com.kroma.ecommerce.model.*;
import com.kroma.ecommerce.model.repository.GarantiaTonoRepository;
import com.kroma.ecommerce.model.repository.PedidoRepository;
import com.kroma.ecommerce.model.repository.ProductoRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Controlador PedidoController (Hereda de BaseController).
 * Gestiona carrito (RF04), checkout colombiano (RF05, RD01, RD03, RF07)
 * y solicitudes de Garantía de Tono de 7 días (RF06).
 */
@Controller
public class PedidoController extends BaseController {

    private static final List<String> DEPARTAMENTOS_CO = List.of(
            "Cundinamarca", "Antioquia", "Valle del Cauca", "Atlántico",
            "Santander", "Bolívar", "Risaralda", "Caldas", "Boyacá", "Tolima", "Nariño", "Meta"
    );

    private final ProductoRepository productoRepo;
    private final PedidoRepository pedidoRepo;
    private final GarantiaTonoRepository garantiaRepo;

    public PedidoController(ProductoRepository productoRepo,
                            PedidoRepository pedidoRepo,
                            GarantiaTonoRepository garantiaRepo) {
        this.productoRepo = productoRepo;
        this.pedidoRepo = pedidoRepo;
        this.garantiaRepo = garantiaRepo;
    }

    @GetMapping("/api/carrito")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> verCarrito(HttpSession session) {
        return ok(resumenCarrito(obtenerCarrito(session)));
    }

    @PostMapping("/api/carrito/agregar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> agregarAlCarrito(@RequestBody Map<String, Object> body,
                                                                HttpSession session) {
        String productoId = String.valueOf(body.getOrDefault("productoId", ""));
        int cantidad = Math.max(1, Integer.parseInt(String.valueOf(body.getOrDefault("cantidad", 1))));

        Optional<Producto> opt = productoRepo.findById(productoId);
        if (opt.isEmpty() || !opt.get().isActivo()) {
            return fail("Producto no disponible en el catálogo.", 404);
        }
        Producto prod = opt.get();
        if (prod.getStock() < cantidad) {
            return fail("Solo hay " + prod.getStock() + " unidades disponibles.", 400);
        }

        String tonoReq = body.get("tonoSeleccionado") != null ? String.valueOf(body.get("tonoSeleccionado")) : null;
        String hexReq = body.get("hexTono") != null ? String.valueOf(body.get("hexTono")) : null;
        String tonoFinal = (tonoReq != null && !tonoReq.isBlank()) ? tonoReq
                : (!prod.getPaletaTonos().isEmpty() ? prod.getPaletaTonos().get(0).getNombreTono() : "Universal");
        String hexFinal = (hexReq != null && !hexReq.isBlank()) ? hexReq
                : (!prod.getPaletaTonos().isEmpty() ? prod.getPaletaTonos().get(0).getHex() : "#E8B4A0");

        List<ItemCarrito> carrito = obtenerCarrito(session);
        Optional<ItemCarrito> existente = carrito.stream()
                .filter(i -> i.getProductoId().equals(prod.getId()) && i.getTonoSeleccionado().equals(tonoFinal))
                .findFirst();

        if (existente.isPresent()) {
            if (existente.get().getCantidad() + cantidad > prod.getStock()) {
                return fail("Stock máximo alcanzado (" + prod.getStock() + " unidades).", 400);
            }
            existente.get().setCantidad(existente.get().getCantidad() + cantidad);
        } else {
            carrito.add(new ItemCarrito(prod.getId(), prod.getCodigo(), prod.getNombre(),
                    prod.getImagenUrl(), tonoFinal, hexFinal, prod.getPrecio(), cantidad));
        }

        Map<String, Object> res = resumenCarrito(carrito);
        res.put("mensaje", "Producto agregado a tu bolsa Kroma.");
        return ok(res);
    }

    @PutMapping("/api/carrito/actualizar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> actualizarCantidad(@RequestBody Map<String, Object> body,
                                                                  HttpSession session) {
        String productoId = String.valueOf(body.getOrDefault("productoId", ""));
        String tono = String.valueOf(body.getOrDefault("tonoSeleccionado", ""));
        int cantidad = Integer.parseInt(String.valueOf(body.getOrDefault("cantidad", 1)));

        List<ItemCarrito> carrito = obtenerCarrito(session);
        Optional<ItemCarrito> optItem = carrito.stream()
                .filter(i -> i.getProductoId().equals(productoId) && i.getTonoSeleccionado().equals(tono))
                .findFirst();

        if (optItem.isEmpty()) {
            return fail("Ítem no encontrado en la bolsa.", 404);
        }

        if (cantidad <= 0) {
            carrito.remove(optItem.get());
        } else {
            Producto prod = productoRepo.findById(productoId).orElse(null);
            if (prod != null && cantidad > prod.getStock()) {
                return fail("Stock disponible: " + prod.getStock(), 400);
            }
            optItem.get().setCantidad(cantidad);
        }
        return ok(resumenCarrito(carrito));
    }

    @DeleteMapping("/api/carrito/eliminar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> eliminarDelCarrito(@RequestBody Map<String, Object> body,
                                                                  HttpSession session) {
        String productoId = String.valueOf(body.getOrDefault("productoId", ""));
        String tono = String.valueOf(body.getOrDefault("tonoSeleccionado", ""));
        List<ItemCarrito> carrito = obtenerCarrito(session);
        carrito.removeIf(i -> i.getProductoId().equals(productoId) && i.getTonoSeleccionado().equals(tono));
        return ok(resumenCarrito(carrito));
    }

    @GetMapping("/api/envio/cotizar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> cotizarEnvio(@RequestParam(defaultValue = "Cundinamarca") String departamento,
                                                            HttpSession session) {
        long subtotal = subtotalCarrito(obtenerCarrito(session));
        long envio = Pedido.calcularCostoEnvio(departamento, subtotal);
        return ok(Map.of("departamento", departamento, "subtotal", subtotal, "costoEnvio", envio, "total", subtotal + envio));
    }

    @GetMapping("/checkout")
    public String mostrarCheckout(HttpSession session, Model model) {
        List<ItemCarrito> carrito = obtenerCarrito(session);
        long subtotal = subtotalCarrito(carrito);
        model.addAttribute("titulo", "Finalizar Compra Sostenible | Kroma Cosmetics");
        model.addAttribute("itemsCarrito", carrito);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("costoEnvioInicial", Pedido.calcularCostoEnvio("Cundinamarca", subtotal));
        model.addAttribute("departamentos", DEPARTAMENTOS_CO);
        model.addAttribute("pedidoConfirmado", null);
        model.addAttribute("error", null);
        return "client/checkout";
    }

    @PostMapping("/checkout")
    public String confirmarPedido(@RequestParam String nombre,
                                  @RequestParam String cedula,
                                  @RequestParam String correo,
                                  @RequestParam String telefono,
                                  @RequestParam String direccion,
                                  @RequestParam String ciudad,
                                  @RequestParam String departamento,
                                  @RequestParam(defaultValue = "") String notas,
                                  @RequestParam String metodoPago,
                                  @RequestParam String tipoEmpaqueSostenible,
                                  @RequestParam(defaultValue = "false") boolean aceptoLey1480,
                                  HttpSession session,
                                  Model model) {
        List<ItemCarrito> carrito = obtenerCarrito(session);
        try {
            if (carrito.isEmpty()) {
                throw new IllegalArgumentException("Tu bolsa de compras está vacía.");
            }

            List<DetallePedido> detalles = new ArrayList<>();
            for (ItemCarrito item : carrito) {
                Producto prod = productoRepo.findById(item.getProductoId())
                        .orElseThrow(() -> new IllegalArgumentException("El producto " + item.getNombreProducto() + " ya no existe."));
                prod.descontarStock(item.getCantidad());
                productoRepo.save(prod);

                detalles.add(new DetallePedido(prod.getId(), prod.getCodigo(), prod.getNombre(),
                        item.getTonoSeleccionado(), item.getHexTono(), item.getCantidad(), prod.getPrecio()));
            }

            Pedido pedido = new Pedido();
            Map<String, Object> actual = usuarioActual(session);
            if (actual != null) {
                pedido.setClienteId(String.valueOf(actual.get("id")));
            }
            pedido.setDatosCliente(new Pedido.DatosCliente(nombre, cedula, correo, telefono));
            pedido.setDireccionEntrega(new Pedido.DireccionEntrega(direccion, ciudad, departamento, notas));
            pedido.setItems(detalles);
            pedido.setMetodoPago(metodoPago);
            pedido.setTipoEmpaqueSostenible(tipoEmpaqueSostenible);
            pedido.setAceptoLey1480(aceptoLey1480);
            pedido.validar();

            Pedido guardado = pedidoRepo.save(pedido);
            carrito.clear();
            session.setAttribute("ultimoCorreoPedido", guardado.getDatosCliente().getCorreo());

            model.addAttribute("titulo", "¡Compra Confirmada! | Kroma Cosmetics");
            model.addAttribute("itemsCarrito", List.of());
            model.addAttribute("subtotal", 0L);
            model.addAttribute("costoEnvioInicial", 0L);
            model.addAttribute("departamentos", DEPARTAMENTOS_CO);
            model.addAttribute("pedidoConfirmado", guardado);
            model.addAttribute("error", null);
            return "client/checkout";
        } catch (Exception e) {
            long subtotal = subtotalCarrito(carrito);
            model.addAttribute("titulo", "Finalizar Compra Sostenible | Kroma Cosmetics");
            model.addAttribute("itemsCarrito", carrito);
            model.addAttribute("subtotal", subtotal);
            model.addAttribute("costoEnvioInicial", Pedido.calcularCostoEnvio("Cundinamarca", subtotal));
            model.addAttribute("departamentos", DEPARTAMENTOS_CO);
            model.addAttribute("pedidoConfirmado", null);
            model.addAttribute("error", e.getMessage());
            return "client/checkout";
        }
    }

    @GetMapping("/mis-pedidos")
    public String mostrarMisPedidos(@RequestParam(required = false) String correo,
                                    HttpSession session,
                                    Model model) {
        Map<String, Object> actual = usuarioActual(session);
        String correoConsulta = correo != null ? correo.trim().toLowerCase()
                : (actual != null ? String.valueOf(actual.get("correo"))
                : String.valueOf(session.getAttribute("ultimoCorreoPedido") != null ? session.getAttribute("ultimoCorreoPedido") : ""));

        List<Pedido> pedidos = List.of();
        List<GarantiaTono> garantias = List.of();

        if (actual != null && "cliente".equals(actual.get("rol"))) {
            String uid = String.valueOf(actual.get("id"));
            String ucorreo = String.valueOf(actual.get("correo"));
            pedidos = pedidoRepo.findByClienteIdOrDatosClienteCorreoIgnoreCaseOrderByFechaPedidoDesc(uid, ucorreo);
            garantias = garantiaRepo.findByClienteIdOrCorreoClienteIgnoreCaseOrderByFechaSolicitudDesc(uid, ucorreo);
        } else if (!correoConsulta.isBlank()) {
            pedidos = pedidoRepo.findByDatosClienteCorreoIgnoreCaseOrderByFechaPedidoDesc(correoConsulta);
            garantias = garantiaRepo.findByCorreoClienteIgnoreCaseOrderByFechaSolicitudDesc(correoConsulta);
        }

        model.addAttribute("titulo", "Mis Pedidos y Garantía de Tono | Kroma Cosmetics");
        model.addAttribute("pedidos", pedidos);
        model.addAttribute("garantias", garantias);
        model.addAttribute("correoConsulta", correoConsulta);
        return "client/mis-pedidos";
    }

    @PostMapping("/api/garantias")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> solicitarGarantia(@RequestBody Map<String, String> body,
                                                                 HttpSession session) {
        try {
            String pedidoId = body.get("pedidoId");
            Pedido pedido = pedidoRepo.findById(pedidoId)
                    .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado."));

            GarantiaTono garantia = new GarantiaTono();
            garantia.setPedidoId(pedido.getId());
            garantia.setNumeroPedido(pedido.getNumeroPedido());
            Map<String, Object> actual = usuarioActual(session);
            garantia.setClienteId(actual != null ? String.valueOf(actual.get("id")) : pedido.getClienteId());
            garantia.setCorreoCliente(pedido.getDatosCliente().getCorreo());
            garantia.setProductoNombre(body.get("productoNombre"));
            garantia.setTonoOriginal(body.get("tonoOriginal"));
            garantia.setTonoSolicitado(body.get("tonoSolicitado"));
            garantia.setMotivo(body.get("motivo"));
            garantia.setFechaEntregaPedido(pedido.getFechaEntregaReal() != null ? pedido.getFechaEntregaReal() : pedido.getFechaPedido());
            garantia.validar();

            GarantiaTono guardada = garantiaRepo.save(garantia);
            return ResponseEntity.status(201).body(Map.of(
                    "ok", true,
                    "mensaje", "Solicitud " + guardada.getCodigoSolicitud() + " radicada dentro del plazo de 7 días.",
                    "garantia", guardada
            ));
        } catch (Exception e) {
            return fail(e.getMessage(), 400);
        }
    }

    private long subtotalCarrito(List<ItemCarrito> carrito) {
        return carrito.stream().mapToLong(ItemCarrito::getSubtotal).sum();
    }

    private Map<String, Object> resumenCarrito(List<ItemCarrito> carrito) {
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("items", carrito);
        res.put("subtotal", subtotalCarrito(carrito));
        res.put("cantidadTotal", carrito.stream().mapToInt(ItemCarrito::getCantidad).sum());
        return res;
    }
}
