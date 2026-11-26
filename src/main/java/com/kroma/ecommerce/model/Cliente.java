package com.kroma.ecommerce.model;

import org.springframework.data.annotation.TypeAlias;

import java.util.List;

/**
 * Clase Cliente (Hereda de Usuario).
 * Modela al comprador de Kroma Cosmetics S.A.S. con datos de envío nacional
 * y su perfil de tono guardado desde el Tone Matcher.
 */
@TypeAlias("cliente")
public class Cliente extends Usuario {

    private String cedula = "";
    private String telefono = "";
    private String direccion = "";
    private String ciudad = "Bogotá";
    private String departamento = "Cundinamarca";
    private PreferenciaTono preferenciaTono = new PreferenciaTono();

    public Cliente() {
        super();
        setRol("cliente");
    }

    public Cliente(String nombre, String correo, String cedula, String telefono,
                   String direccion, String ciudad, String departamento) {
        super(nombre, correo, "cliente");
        this.cedula = cedula != null ? cedula.trim() : "";
        this.telefono = telefono != null ? telefono.trim() : "";
        this.direccion = direccion != null ? direccion.trim() : "";
        this.ciudad = (ciudad != null && !ciudad.isBlank()) ? ciudad.trim() : "Bogotá";
        this.departamento = (departamento != null && !departamento.isBlank()) ? departamento.trim() : "Cundinamarca";
    }

    public String getCedula() { return cedula; }
    public void setCedula(String cedula) { this.cedula = cedula; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }

    public String getDepartamento() { return departamento; }
    public void setDepartamento(String departamento) { this.departamento = departamento; }

    public PreferenciaTono getPreferenciaTono() { return preferenciaTono; }
    public void setPreferenciaTono(PreferenciaTono preferenciaTono) { this.preferenciaTono = preferenciaTono; }

    @Override
    public List<String> getPermisos() {
        return List.of(
                "ver_catalogo",
                "usar_tone_matcher",
                "gestionar_carrito",
                "realizar_compra",
                "solicitar_garantia_tono",
                "ver_mis_pedidos"
        );
    }

    @Override
    public String getPanelRuta() {
        return "/mis-pedidos";
    }

    public static class PreferenciaTono {
        private String subtono = "Sin definir";
        private String tipoPiel = "Sin definir";
        private String tonoSugerido = "";

        public PreferenciaTono() {
        }

        public PreferenciaTono(String subtono, String tipoPiel, String tonoSugerido) {
            this.subtono = subtono;
            this.tipoPiel = tipoPiel;
            this.tonoSugerido = tonoSugerido;
        }

        public String getSubtono() { return subtono; }
        public void setSubtono(String subtono) { this.subtono = subtono; }

        public String getTipoPiel() { return tipoPiel; }
        public void setTipoPiel(String tipoPiel) { this.tipoPiel = tipoPiel; }

        public String getTonoSugerido() { return tonoSugerido; }
        public void setTonoSugerido(String tonoSugerido) { this.tonoSugerido = tonoSugerido; }
    }
}
