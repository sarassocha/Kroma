package com.kroma.ecommerce.model;

import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Clase GarantiaTono (Hereda de EntidadBase).
 * Administra solicitudes gratuitas de cambio de tono dentro de los 7 días reglamentarios (RF06).
 */
@Document(collection = "garantias")
public class GarantiaTono extends EntidadBase {

    public static final int PLAZO_MAXIMO_DIAS = 7;

    @Indexed(unique = true)
    private String codigoSolicitud = "GAR-" + ThreadLocalRandom.current().nextInt(10000, 99999);
    private String pedidoId;
    private String numeroPedido;
    private String clienteId;
    private String correoCliente;
    private String productoNombre;
    private String tonoOriginal;
    private String tonoSolicitado;
    private String motivo;
    private String estadoSolicitud = "Pendiente";
    private String empaqueRetorno = "Bolsa Compostable de Retorno Sin Costo";
    private Instant fechaSolicitud = Instant.now();

    @Transient
    private Instant fechaEntregaPedido = Instant.now();

    public GarantiaTono() {
    }

    public boolean estaDentroDePlazo() {
        if (fechaEntregaPedido == null || fechaSolicitud == null) {
            return false;
        }
        long horas = Duration.between(fechaEntregaPedido, fechaSolicitud).toHours();
        return horas >= 0 && horas <= PLAZO_MAXIMO_DIAS * 24L;
    }

    @Override
    public void validar() {
        List<String> errores = new ArrayList<>();
        if (pedidoId == null || pedidoId.isBlank() || numeroPedido == null || numeroPedido.isBlank()) {
            errores.add("La referencia del pedido es obligatoria.");
        }
        if (productoNombre == null || productoNombre.isBlank()
                || tonoOriginal == null || tonoOriginal.isBlank()
                || tonoSolicitado == null || tonoSolicitado.isBlank()) {
            errores.add("Debes indicar producto, tono original y nuevo tono deseado.");
        } else if (tonoOriginal.trim().equalsIgnoreCase(tonoSolicitado.trim())) {
            errores.add("El nuevo tono solicitado debe ser diferente al tono recibido.");
        }
        if (motivo == null || motivo.trim().length() < 6) {
            errores.add("Describe el motivo del cambio (mínimo 6 caracteres).");
        }
        if (!estaDentroDePlazo()) {
            errores.add("RF06: El plazo máximo de " + PLAZO_MAXIMO_DIAS + " días para Garantía de Tono ha expirado.");
        }
        if (!errores.isEmpty()) {
            throw new IllegalArgumentException(String.join(" ", errores));
        }
    }

    public String getCodigoSolicitud() { return codigoSolicitud; }
    public void setCodigoSolicitud(String codigoSolicitud) { this.codigoSolicitud = codigoSolicitud; }

    public String getPedidoId() { return pedidoId; }
    public void setPedidoId(String pedidoId) { this.pedidoId = pedidoId; }

    public String getNumeroPedido() { return numeroPedido; }
    public void setNumeroPedido(String numeroPedido) { this.numeroPedido = numeroPedido; }

    public String getClienteId() { return clienteId; }
    public void setClienteId(String clienteId) { this.clienteId = clienteId; }

    public String getCorreoCliente() { return correoCliente; }
    public void setCorreoCliente(String correoCliente) { this.correoCliente = correoCliente != null ? correoCliente.trim().toLowerCase() : ""; }

    public String getProductoNombre() { return productoNombre; }
    public void setProductoNombre(String productoNombre) { this.productoNombre = productoNombre; }

    public String getTonoOriginal() { return tonoOriginal; }
    public void setTonoOriginal(String tonoOriginal) { this.tonoOriginal = tonoOriginal; }

    public String getTonoSolicitado() { return tonoSolicitado; }
    public void setTonoSolicitado(String tonoSolicitado) { this.tonoSolicitado = tonoSolicitado; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public String getEstadoSolicitud() { return estadoSolicitud; }
    public void setEstadoSolicitud(String estadoSolicitud) { this.estadoSolicitud = estadoSolicitud; }

    public String getEmpaqueRetorno() { return empaqueRetorno; }
    public void setEmpaqueRetorno(String empaqueRetorno) { this.empaqueRetorno = empaqueRetorno; }

    public Instant getFechaSolicitud() { return fechaSolicitud; }
    public void setFechaSolicitud(Instant fechaSolicitud) { this.fechaSolicitud = fechaSolicitud; }

    public Instant getFechaEntregaPedido() { return fechaEntregaPedido; }
    public void setFechaEntregaPedido(Instant fechaEntregaPedido) { this.fechaEntregaPedido = fechaEntregaPedido; }
}
