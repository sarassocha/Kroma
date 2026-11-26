package com.kroma.ecommerce.model;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Clase Abstracta Usuario (Hereda de EntidadBase).
 * Encapsula las credenciales con encriptación BCrypt (RNF03) y define
 * comportamiento polimórfico para Cliente y Administrador.
 */
@Document(collection = "usuarios")
public abstract class Usuario extends EntidadBase {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private String nombre;

    @Indexed(unique = true)
    private String correo;

    private String passwordHash;
    private String rol;

    protected Usuario() {
    }

    protected Usuario(String nombre, String correo, String rol) {
        this.nombre = nombre != null ? nombre.trim() : "";
        this.correo = correo != null ? correo.trim().toLowerCase() : "";
        this.rol = rol;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre != null ? nombre.trim() : "";
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo != null ? correo.trim().toLowerCase() : "";
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    /**
     * Encripta la contraseña en texto plano mediante BCrypt (RNF03).
     */
    public void establecerPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.length() < 6) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres.");
        }
        this.passwordHash = ENCODER.encode(plainPassword);
    }

    /**
     * Verifica la contraseña ingresada frente al hash BCrypt almacenado.
     */
    public boolean verificarPassword(String plainPassword) {
        if (plainPassword == null || this.passwordHash == null || this.passwordHash.isBlank()) {
            return false;
        }
        return ENCODER.matches(plainPassword, this.passwordHash);
    }

    @Override
    public void validar() {
        if (nombre == null || nombre.length() < 2) {
            throw new IllegalArgumentException("El nombre completo es obligatorio.");
        }
        if (correo == null || !EMAIL_PATTERN.matcher(correo).matches()) {
            throw new IllegalArgumentException("El correo electrónico no tiene un formato válido.");
        }
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("La contraseña encriptada es obligatoria.");
        }
    }

    public abstract List<String> getPermisos();

    public abstract String getPanelRuta();
}
