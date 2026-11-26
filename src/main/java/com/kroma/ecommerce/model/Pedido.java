package com.kroma.ecommerce.model;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Clase Pedido (Hereda de EntidadBase, compone DetallePedido).
 * Implementa logística colombiana (RD03), cumplimiento Ley 1480 de 2011 (RD01),
 * trazabilidad de empaque sostenible (RF07) y validación de plazo de garantía (RF06).
 */
@Document(collection = "pedidos")
public class Pedido extends EntidadBase {

    public static final long ENVIO_GRATIS_DESDE = 120_000L;
    public static final long TARIFA_NACIONAL_ESTANDAR = 14_900L;
    public static final Map<String, Long> TARIFAS_DEPARTAMENTO = Map.of(
            "Cundinamarca", 8_900L,
            "Antioquia", 11_900L,
            "Valle del Cauca", 11_900L,
            "Atlántico", 13_500L,
            "Santander", 12_500L,
            "Bolívar", 13_900L,
            "Risaralda", 11_500L,
            "Caldas", 11_500L
    );

    @Indexed(unique = true)
    private String numeroPedido = generarCodigo("KRM");
    private String clienteId;
    private DatosCliente datosCliente = new DatosCliente();
    private DireccionEntrega direccionEntrega = new DireccionEntrega();
    private List<DetallePedido> items = new ArrayList<>();
    private long subtotalProductos;
    private long costoEnvio;
    private long total;
    private String metodoPago;
    private String estadoPago = "Aprobado";
    private String comprobantePago = generarCodigo("PAY");
    private String tipoEmpaqueSostenible;
    private String numeroGuia = generarCodigo("ECO-GUIA");
    private String supervisorEmpaque = "Laura Martínez (Eco-Logística)";
    private String estadoPedido = "Pagado";
    private boolean aceptoLey1480;
    private Instant fechaPedido = Instant.now();
    private Instant fechaEntregaEstimada = Instant.now().plus(Duration.ofDays(3));
    private Instant fechaEntregaReal;

    public Pedido() {
    }

    public static String generarCodigo(String prefijo) {
        int rand = ThreadLocalRandom.current().nextInt(100000, 999999);
        return prefijo + "-" + rand;
    }

    public static long calcularCostoEnvio(String departamento, long subtotal) {
        if (subtotal >= ENVIO_GRATIS_DESDE) {
            return 0L;
        }
        return TARIFAS_DEPARTAMENTO.getOrDefault(departamento, TARIFA_NACIONAL_ESTANDAR);
    }

    public long calcularSubtotalProductos() {
        this.subtotalProductos = items.stream().mapToLong(DetallePedido::calcularSubtotal).sum();
        return this.subtotalProductos;
    }

    public long calcularTotal() {
        long sub = calcularSubtotalProductos();
        String depto = direccionEntrega != null ? direccionEntrega.getDepartamento() : "";
        this.costoEnvio = calcularCostoEnvio(depto, sub);
        this.total = sub + this.costoEnvio;
        return this.total;
    }

    public boolean estaEnPlazoGarantia() {
        return estaEnPlazoGarantia(Instant.now());
    }

    public boolean estaEnPlazoGarantia(Instant fechaActual) {
        Instant referencia = fechaEntregaReal != null ? fechaEntregaReal : fechaPedido;
        long horas = Duration.between(referencia, fechaActual).toHours();
        return horas >= 0 && horas <= 7 * 24L;
    }

    @Override
    public void validar() {
        List<String> errores = new ArrayList<>();
        if (datosCliente == null || isBlank(datosCliente.getNombre()) || isBlank(datosCliente.getCedula())
                || isBlank(datosCliente.getCorreo()) || isBlank(datosCliente.getTelefono())) {
            errores.add("Todos los datos del cliente (nombre, cédula, correo y teléfono) son obligatorios.");
        }
        if (direccionEntrega == null || isBlank(direccionEntrega.getDireccion())
                || isBlank(direccionEntrega.getCiudad()) || isBlank(direccionEntrega.getDepartamento())) {
            errores.add("La dirección completa en Colombia es obligatoria.");
        }
        if (items == null || items.isEmpty()) {
            errores.add("El carrito debe tener al menos un producto.");
        }
        if (!aceptoLey1480) {
            errores.add("RD01: Debes aceptar los términos del Estatuto del Consumidor (Ley 1480 de 2011).");
        }
        if (isBlank(tipoEmpaqueSostenible)) {
            errores.add("RF07: Debes seleccionar un tipo de empaque sostenible.");
        }
        if (!errores.isEmpty()) {
            throw new IllegalArgumentException(String.join(" ", errores));
        }
        calcularTotal();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public String getNumeroPedido() { return numeroPedido; }
    public void setNumeroPedido(String numeroPedido) { this.numeroPedido = numeroPedido; }

    public String getClienteId() { return clienteId; }
    public void setClienteId(String clienteId) { this.clienteId = clienteId; }

    public DatosCliente getDatosCliente() { return datosCliente; }
    public void setDatosCliente(DatosCliente datosCliente) { this.datosCliente = datosCliente; }

    public DireccionEntrega getDireccionEntrega() { return direccionEntrega; }
    public void setDireccionEntrega(DireccionEntrega direccionEntrega) { this.direccionEntrega = direccionEntrega; }

    public List<DetallePedido> getItems() { return items; }
    public void setItems(List<DetallePedido> items) { this.items = items != null ? items : new ArrayList<>(); }

    public long getSubtotalProductos() { return subtotalProductos; }
    public void setSubtotalProductos(long subtotalProductos) { this.subtotalProductos = subtotalProductos; }

    public long getCostoEnvio() { return costoEnvio; }
    public void setCostoEnvio(long costoEnvio) { this.costoEnvio = costoEnvio; }

    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }

    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }

    public String getEstadoPago() { return estadoPago; }
    public void setEstadoPago(String estadoPago) { this.estadoPago = estadoPago; }

    public String getComprobantePago() { return comprobantePago; }
    public void setComprobantePago(String comprobantePago) { this.comprobantePago = comprobantePago; }

    public String getTipoEmpaqueSostenible() { return tipoEmpaqueSostenible; }
    public void setTipoEmpaqueSostenible(String tipoEmpaqueSostenible) { this.tipoEmpaqueSostenible = tipoEmpaqueSostenible; }

    public String getNumeroGuia() { return numeroGuia; }
    public void setNumeroGuia(String numeroGuia) { this.numeroGuia = numeroGuia; }

    public String getSupervisorEmpaque() { return supervisorEmpaque; }
    public void setSupervisorEmpaque(String supervisorEmpaque) { this.supervisorEmpaque = supervisorEmpaque; }

    public String getEstadoPedido() { return estadoPedido; }
    public void setEstadoPedido(String estadoPedido) { this.estadoPedido = estadoPedido; }

    public boolean isAceptoLey1480() { return aceptoLey1480; }
    public void setAceptoLey1480(boolean aceptoLey1480) { this.aceptoLey1480 = aceptoLey1480; }

    public Instant getFechaPedido() { return fechaPedido; }
    public void setFechaPedido(Instant fechaPedido) { this.fechaPedido = fechaPedido; }

    public Instant getFechaEntregaEstimada() { return fechaEntregaEstimada; }
    public void setFechaEntregaEstimada(Instant fechaEntregaEstimada) { this.fechaEntregaEstimada = fechaEntregaEstimada; }

    public Instant getFechaEntregaReal() { return fechaEntregaReal; }
    public void setFechaEntregaReal(Instant fechaEntregaReal) { this.fechaEntregaReal = fechaEntregaReal; }

    public static class DatosCliente {
        private String nombre;
        private String cedula;
        private String correo;
        private String telefono;

        public DatosCliente() {
        }

        public DatosCliente(String nombre, String cedula, String correo, String telefono) {
            this.nombre = nombre;
            this.cedula = cedula;
            this.correo = correo != null ? correo.trim().toLowerCase() : "";
            this.telefono = telefono;
        }

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }

        public String getCedula() { return cedula; }
        public void setCedula(String cedula) { this.cedula = cedula; }

        public String getCorreo() { return correo; }
        public void setCorreo(String correo) { this.correo = correo != null ? correo.trim().toLowerCase() : ""; }

        public String getTelefono() { return telefono; }
        public void setTelefono(String telefono) { this.telefono = telefono; }
    }

    public static class DireccionEntrega {
        private String direccion;
        private String ciudad;
        private String departamento;
        private String notas = "";

        public DireccionEntrega() {
        }

        public DireccionEntrega(String direccion, String ciudad, String departamento, String notas) {
            this.direccion = direccion;
            this.ciudad = ciudad;
            this.departamento = departamento;
            this.notas = notas != null ? notas : "";
        }

        public String getDireccion() { return direccion; }
        public void setDireccion(String direccion) { this.direccion = direccion; }

        public String getCiudad() { return ciudad; }
        public void setCiudad(String ciudad) { this.ciudad = ciudad; }

        public String getDepartamento() { return departamento; }
        public void setDepartamento(String departamento) { this.departamento = departamento; }

        public String getNotas() { return notas; }
        public void setNotas(String notas) { this.notas = notas; }
    }
}
