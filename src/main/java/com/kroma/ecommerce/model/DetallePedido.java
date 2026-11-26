package com.kroma.ecommerce.model;

/**
 * Clase DetallePedido (Composición dentro de Pedido - RS01).
 */
public class DetallePedido {

    private String productoId;
    private String codigoProducto;
    private String nombreProducto;
    private String tonoSeleccionado = "Universal";
    private String hexTono = "#E8B4A0";
    private int cantidad;
    private long precioUnitario;
    private long subtotal;

    public DetallePedido() {
    }

    public DetallePedido(String productoId, String codigoProducto, String nombreProducto,
                         String tonoSeleccionado, String hexTono, int cantidad, long precioUnitario) {
        if (nombreProducto == null || nombreProducto.isBlank()) {
            throw new IllegalArgumentException("El nombre del producto en el detalle es obligatorio.");
        }
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser al menos 1.");
        }
        if (precioUnitario < 0) {
            throw new IllegalArgumentException("El precio unitario no es válido.");
        }
        this.productoId = productoId;
        this.codigoProducto = codigoProducto;
        this.nombreProducto = nombreProducto.trim();
        this.tonoSeleccionado = (tonoSeleccionado != null && !tonoSeleccionado.isBlank()) ? tonoSeleccionado : "Universal";
        this.hexTono = (hexTono != null && !hexTono.isBlank()) ? hexTono : "#E8B4A0";
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = calcularSubtotal();
    }

    public long calcularSubtotal() {
        this.subtotal = this.cantidad * this.precioUnitario;
        return this.subtotal;
    }

    public String getProductoId() { return productoId; }
    public void setProductoId(String productoId) { this.productoId = productoId; }

    public String getCodigoProducto() { return codigoProducto; }
    public void setCodigoProducto(String codigoProducto) { this.codigoProducto = codigoProducto; }

    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }

    public String getTonoSeleccionado() { return tonoSeleccionado; }
    public void setTonoSeleccionado(String tonoSeleccionado) { this.tonoSeleccionado = tonoSeleccionado; }

    public String getHexTono() { return hexTono; }
    public void setHexTono(String hexTono) { this.hexTono = hexTono; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public long getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(long precioUnitario) { this.precioUnitario = precioUnitario; }

    public long getSubtotal() { return subtotal; }
    public void setSubtotal(long subtotal) { this.subtotal = subtotal; }
}
