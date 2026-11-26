package com.kroma.ecommerce.model;

import org.springframework.data.annotation.Id;

import java.time.Instant;

/**
 * Clase Abstracta EntidadBase (RS01 - POO).
 * Define el contrato base de identidad, auditoría temporal y validación polimórfica
 * para todas las entidades del dominio de Kroma Cosmetics S.A.S.
 */
public abstract class EntidadBase {

    @Id
    private String id;
    private Instant fechaCreacion = Instant.now();

    protected EntidadBase() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Instant getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Instant fechaCreacion) {
        this.fechaCreacion = fechaCreacion != null ? fechaCreacion : Instant.now();
    }

    /**
     * Método polimórfico obligatorio para validar reglas de negocio de cada entidad.
     */
    public abstract void validar();
}
