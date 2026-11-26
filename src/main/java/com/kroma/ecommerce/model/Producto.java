package com.kroma.ecommerce.model;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * Clase Producto (Hereda de EntidadBase).
 * Modela los productos skin-first de Kroma y valida cumplimiento estricto de RD02
 * (Certificación Cruelty-Free y formulación libre de ingredientes abrasivos).
 */
@Document(collection = "productos")
public class Producto extends EntidadBase {

    public static final List<String> INGREDIENTES_PROHIBIDOS = List.of(
            "parabenos",
            "sulfatos agresivos",
            "microesferas plásticas",
            "hidroquinona",
            "mercurio",
            "aceite mineral pesado",
            "testeo animal"
    );

    @Indexed(unique = true)
    private String codigo;
    private String nombre;
    private String descripcionCorta;
    private String descripcionIngredientes;
    private boolean certificacionCrueltyFree = true;
    private boolean libreDeAbrasivos = true;
    private String categoria;
    private String linea = "Skin-First Nude";
    private long precio;
    private long precioAnterior = 0;
    private int stock;
    private List<String> tipoPiel = new ArrayList<>(List.of("Todo tipo"));
    private String gamaTono = "Universal";
    private List<TonoSwatch> paletaTonos = new ArrayList<>();
    private String imagenUrl = "/images/product-showcase.jpg";
    private boolean destacado = false;
    private boolean activo = true;

    public Producto() {
    }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo != null ? codigo.trim().toUpperCase() : ""; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre != null ? nombre.trim() : ""; }

    public String getDescripcionCorta() { return descripcionCorta; }
    public void setDescripcionCorta(String descripcionCorta) { this.descripcionCorta = descripcionCorta != null ? descripcionCorta.trim() : ""; }

    public String getDescripcionIngredientes() { return descripcionIngredientes; }
    public void setDescripcionIngredientes(String descripcionIngredientes) { this.descripcionIngredientes = descripcionIngredientes != null ? descripcionIngredientes.trim() : ""; }

    public boolean isCertificacionCrueltyFree() { return certificacionCrueltyFree; }
    public void setCertificacionCrueltyFree(boolean certificacionCrueltyFree) { this.certificacionCrueltyFree = certificacionCrueltyFree; }

    public boolean isLibreDeAbrasivos() { return libreDeAbrasivos; }
    public void setLibreDeAbrasivos(boolean libreDeAbrasivos) { this.libreDeAbrasivos = libreDeAbrasivos; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getLinea() { return linea; }
    public void setLinea(String linea) { this.linea = linea; }

    public long getPrecio() { return precio; }
    public void setPrecio(long precio) { this.precio = precio; }

    public long getPrecioAnterior() { return precioAnterior; }
    public void setPrecioAnterior(long precioAnterior) { this.precioAnterior = precioAnterior; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public List<String> getTipoPiel() { return tipoPiel; }
    public void setTipoPiel(List<String> tipoPiel) {
        this.tipoPiel = (tipoPiel != null && !tipoPiel.isEmpty()) ? tipoPiel : new ArrayList<>(List.of("Todo tipo"));
    }

    public String getGamaTono() { return gamaTono; }
    public void setGamaTono(String gamaTono) { this.gamaTono = gamaTono != null ? gamaTono : "Universal"; }

    public List<TonoSwatch> getPaletaTonos() { return paletaTonos; }
    public void setPaletaTonos(List<TonoSwatch> paletaTonos) { this.paletaTonos = paletaTonos != null ? paletaTonos : new ArrayList<>(); }

    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = (imagenUrl != null && !imagenUrl.isBlank()) ? imagenUrl.trim() : "/images/product-showcase.jpg";
    }

    public boolean isDestacado() { return destacado; }
    public void setDestacado(boolean destacado) { this.destacado = destacado; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public int getDescuentoPorcentaje() {
        if (precioAnterior > precio && precioAnterior > 0) {
            return (int) Math.round((1.0 - ((double) precio / precioAnterior)) * 100);
        }
        return 0;
    }

    public String getPaletaResumenTexto() {
        if (paletaTonos == null || paletaTonos.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < paletaTonos.size(); i++) {
            TonoSwatch t = paletaTonos.get(i);
            if (i > 0) sb.append(", ");
            sb.append(t.getNombreTono()).append("|").append(t.getHex()).append("|").append(t.getSubtono());
        }
        return sb.toString();
    }

    @Override
    public void validar() {
        List<String> errores = new ArrayList<>();
        if (codigo == null || codigo.isBlank()) errores.add("El código SKU del producto es obligatorio.");
        if (nombre == null || nombre.length() < 3) errores.add("El nombre debe tener al menos 3 caracteres.");
        if (descripcionCorta == null || descripcionCorta.isBlank()) errores.add("La descripción corta es obligatoria.");
        if (descripcionIngredientes == null || descripcionIngredientes.length() < 8) {
            errores.add("La descripción de ingredientes skin-first es obligatoria.");
        }
        if (!certificacionCrueltyFree || !libreDeAbrasivos) {
            errores.add("RD02: Todo producto Kroma requiere certificación Cruelty-Free y fórmula libre de abrasivos.");
        }
        if (descripcionIngredientes != null) {
            String lower = descripcionIngredientes.toLowerCase();
            for (String prohibido : INGREDIENTES_PROHIBIDOS) {
                if (lower.contains(prohibido)) {
                    errores.add("RD02: Ingrediente prohibido detectado (" + prohibido + ").");
                }
            }
        }
        if (precio <= 0) errores.add("El precio en COP debe ser mayor a cero.");
        if (stock < 0) errores.add("El stock no puede ser negativo.");
        if (!errores.isEmpty()) {
            throw new IllegalArgumentException(String.join(" ", errores));
        }
    }

    public int descontarStock(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad a descontar debe ser positiva.");
        }
        if (cantidad > this.stock) {
            throw new IllegalArgumentException("Stock insuficiente para '" + nombre + "'. Disponible: " + stock);
        }
        this.stock -= cantidad;
        return this.stock;
    }

    public int reponerStock(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad a reponer debe ser positiva.");
        }
        this.stock += cantidad;
        return this.stock;
    }

    public static class TonoSwatch {
        private String nombreTono;
        private String hex;
        private String subtono;

        public TonoSwatch() {
        }

        public TonoSwatch(String nombreTono, String hex, String subtono) {
            this.nombreTono = nombreTono;
            this.hex = hex;
            this.subtono = subtono;
        }

        public String getNombreTono() { return nombreTono; }
        public void setNombreTono(String nombreTono) { this.nombreTono = nombreTono; }

        public String getHex() { return hex; }
        public void setHex(String hex) { this.hex = hex; }

        public String getSubtono() { return subtono; }
        public void setSubtono(String subtono) { this.subtono = subtono; }
    }
}
