package com.kroma.ecommerce.model;

/**
 * Representa un ítem temporal en la bolsa de compras de la sesión del usuario (RF04).
 */
public class ItemCarrito {

    private String productoId;
    private String codigoProducto;
    private String nombreProducto;
    private String imagenUrl;
    private String tonoSeleccionado;
    private String hexTono;
    private long precioUnitario;
    private int cantidad;

    public ItemCarrito() {
    }

    public ItemCarrito(String productoId, String codigoProducto, String nombreProducto, String imagenUrl,
                       String tonoSeleccionado, String hexTono, long precioUnitario, int cantidad) {
        this.productoId = productoId;
        this.codigoProducto = codigoProducto;
        this.nombreProducto = nombreProducto;
        this.imagenUrl = imagenUrl;
        this.tonoSeleccionado = tonoSeleccionado;
        this.hexTono = hexTono;
        this.precioUnitario = precioUnitario;
        this.cantidad = cantidad;
    }

    public long getSubtotal() {
        return precioUnitario * cantidad;
    }

    public String getProductoId() { return productoId; }
    public void setProductoId(String productoId) { this.productoId = productoId; }

    public String getCodigoProducto() { return codigoProducto; }
    public void setCodigoProducto(String codigoProducto) { this.codigoProducto = codigoProducto; }

    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }

    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }

    public String getTonoSeleccionado() { return tonoSeleccionado; }
    public void setTonoSeleccionado(String tonoSeleccionado) { this.tonoSeleccionado = tonoSeleccionado; }

    public String getHexTono() { return hexTono; }
    public void setHexTono(String hexTono) { this.hexTono = hexTono; }

    public long getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(long precioUnitario) { this.precioUnitario = precioUnitario; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
}
