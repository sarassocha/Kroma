package com.kroma.ecommerce.model;

import org.springframework.data.annotation.TypeAlias;

import java.util.List;

/**
 * Clase Administrador (Hereda de Usuario).
 * Modela al personal autorizado de Kroma (RU03) con permisos para agregar, editar,
 * eliminar productos y consultar el historial completo de compras.
 */
@TypeAlias("admin")
public class Administrador extends Usuario {

    private String codigoEmpleado = "ADM-KRM-001";
    private String area = "Marketing, Operaciones y Garantías";

    public Administrador() {
        super();
        setRol("admin");
    }

    public Administrador(String nombre, String correo, String codigoEmpleado, String area) {
        super(nombre, correo, "admin");
        this.codigoEmpleado = codigoEmpleado != null ? codigoEmpleado.trim() : "ADM-KRM-001";
        this.area = area != null ? area.trim() : "Operaciones & Catálogo";
    }

    public String getCodigoEmpleado() { return codigoEmpleado; }
    public void setCodigoEmpleado(String codigoEmpleado) { this.codigoEmpleado = codigoEmpleado; }

    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }

    @Override
    public List<String> getPermisos() {
        return List.of(
                "ver_catalogo",
                "agregar_producto",
                "editar_producto",
                "eliminar_producto",
                "ver_historial_compras",
                "gestionar_pedidos",
                "gestionar_garantias"
        );
    }

    public boolean tienePermiso(String accion) {
        return getPermisos().contains(accion);
    }

    @Override
    public String getPanelRuta() {
        return "/admin";
    }
}
