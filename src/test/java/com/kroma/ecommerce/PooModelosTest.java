package com.kroma.ecommerce;

import com.kroma.ecommerce.model.*;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 4.2 Pruebas Unitarias de POO y Reglas de Dominio - Kroma Cosmetics S.A.S.
 */
class PooModelosTest {

    @Test
    void herenciaPolimorfismoYBcrypUsuario() {
        Cliente cliente = new Cliente(
                "Sofía Vargas", "sofia@ejemplo.com.co",
                "1020304050", "3001112233", "Cra 7 #45-10", "Bogotá", "Cundinamarca"
        );
        cliente.establecerPassword("ClaveSegura123");
        cliente.validar();

        assertInstanceOf(Usuario.class, cliente);
        assertInstanceOf(EntidadBase.class, cliente);
        assertTrue(cliente.verificarPassword("ClaveSegura123"));
        assertFalse(cliente.verificarPassword("ClaveErronea"));
        assertEquals("/mis-pedidos", cliente.getPanelRuta());
        assertFalse(cliente.getPermisos().contains("eliminar_producto"));

        Administrador admin = new Administrador(
                "Valentina Admin", "admin@kroma.com.co", "ADM-01", "Operaciones"
        );
        admin.establecerPassword("AdminPass2026!");
        admin.validar();

        assertEquals("/admin", admin.getPanelRuta());
        assertTrue(admin.tienePermiso("agregar_producto"));
        assertTrue(admin.tienePermiso("editar_producto"));
        assertTrue(admin.tienePermiso("eliminar_producto"));
        assertTrue(admin.tienePermiso("ver_historial_compras"));
    }

    @Test
    void productoValidaCrueltyFreeYControlStockRD02() {
        Producto invalido = new Producto();
        invalido.setCodigo("KRM-BAD");
        invalido.setNombre("Exfoliante Inválido");
        invalido.setDescripcionCorta("Fórmula no apta");
        invalido.setDescripcionIngredientes("Contiene parabenos y microesferas plásticas");
        invalido.setCertificacionCrueltyFree(false);
        invalido.setCategoria("Skincare");
        invalido.setPrecio(30000);
        invalido.setStock(10);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, invalido::validar);
        assertTrue(ex.getMessage().contains("RD02"));

        Producto valido = new Producto();
        valido.setCodigo("KRM-OK");
        valido.setNombre("Base Sérum Natural");
        valido.setDescripcionCorta("Cobertura ligera skin-first");
        valido.setDescripcionIngredientes("Ácido hialurónico, niacinamida y pigmentos minerales suaves.");
        valido.setCertificacionCrueltyFree(true);
        valido.setLibreDeAbrasivos(true);
        valido.setCategoria("Rostro");
        valido.setPrecio(58000);
        valido.setStock(15);

        assertDoesNotThrow(valido::validar);
        assertEquals(10, valido.descontarStock(5));
        assertThrows(IllegalArgumentException.class, () -> valido.descontarStock(20));
        assertEquals(13, valido.reponerStock(3));
    }

    @Test
    void pedidoValidaLey1480YCalculaEnvioColombiaRD01RD03() {
        DetallePedido detalle = new DetallePedido(
                "prod-1", "KRM-001", "Base Sérum", "02 Arena Nude", "#E3BE9F", 1, 58900
        );
        assertEquals(58900, detalle.getSubtotal());

        Pedido pedidoSinLey = new Pedido();
        pedidoSinLey.setDatosCliente(new Pedido.DatosCliente("Ana", "123", "ana@mail.com", "3001112233"));
        pedidoSinLey.setDireccionEntrega(new Pedido.DireccionEntrega("Cra 7 #45-10", "Bogotá", "Cundinamarca", ""));
        pedidoSinLey.setItems(List.of(detalle));
        pedidoSinLey.setMetodoPago("Nequi");
        pedidoSinLey.setTipoEmpaqueSostenible("Caja Biodegradable Bagazo de Caña");
        pedidoSinLey.setAceptoLey1480(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, pedidoSinLey::validar);
        assertTrue(ex.getMessage().contains("Ley 1480"));

        pedidoSinLey.setAceptoLey1480(true);
        assertDoesNotThrow(pedidoSinLey::validar);
        assertEquals(8900, pedidoSinLey.getCostoEnvio());
        assertEquals(67800, pedidoSinLey.getTotal());
        assertEquals(0, Pedido.calcularCostoEnvio("Antioquia", 150000));
    }

    @Test
    void garantiaTonoValidaPlazoMaximoDe7DiasRF06() {
        GarantiaTono vigente = new GarantiaTono();
        vigente.setPedidoId("ped-1");
        vigente.setNumeroPedido("KRM-104820");
        vigente.setCorreoCliente("camila@ejemplo.com.co");
        vigente.setProductoNombre("Base Sérum");
        vigente.setTonoOriginal("01 Porcelana");
        vigente.setTonoSolicitado("02 Arena Nude");
        vigente.setMotivo("El tono quedó muy claro bajo luz natural.");
        vigente.setFechaEntregaPedido(Instant.now().minus(Duration.ofDays(3)));
        assertDoesNotThrow(vigente::validar);

        GarantiaTono expirada = new GarantiaTono();
        expirada.setPedidoId("ped-1");
        expirada.setNumeroPedido("KRM-104820");
        expirada.setCorreoCliente("camila@ejemplo.com.co");
        expirada.setProductoNombre("Base Sérum");
        expirada.setTonoOriginal("01 Porcelana");
        expirada.setTonoSolicitado("02 Arena Nude");
        expirada.setMotivo("Cambio fuera de plazo.");
        expirada.setFechaEntregaPedido(Instant.now().minus(Duration.ofDays(10)));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, expirada::validar);
        assertTrue(ex.getMessage().contains("7 días"));
    }

    @Test
    void toneMatcherDiagnosticaYRecomiendaProductosRF03() {
        ToneMatcherService service = new ToneMatcherService();
        assertEquals("Cálido", service.diagnosticarSubtono("verdosas", "se_broncea", "dorada").subtono());
        assertEquals("Frío", service.diagnosticarSubtono("azuladas", "se_quema", "plateada").subtono());

        Producto p = new Producto();
        p.setNombre("Base Sérum");
        p.setStock(10);
        p.setActivo(true);
        p.setTipoPiel(List.of("Mixta"));
        p.setGamaTono("Cálido");
        p.setPaletaTonos(List.of(new Producto.TonoSwatch("03 Miel Dorada", "#C9986B", "Cálido")));

        List<ToneMatcherService.Recomendacion> recs = service.recomendarProductos(List.of(p), "Cálido", "Mixta");
        assertEquals(1, recs.size());
        assertEquals("03 Miel Dorada", recs.get(0).getTonoRecomendado().getNombreTono());
    }
}
