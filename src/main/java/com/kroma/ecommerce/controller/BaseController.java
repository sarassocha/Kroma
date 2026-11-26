package com.kroma.ecommerce.controller;

import com.kroma.ecommerce.model.ItemCarrito;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.text.NumberFormat;
import java.util.*;

/**
 * Clase BaseController (POO en la capa Controlador + @ControllerAdvice).
 * Comparte utilidades globales para vistas Thymeleaf y respuestas JSON uniformes.
 */
@ControllerAdvice
public class BaseController {

    private static final Locale LOCALE_CO = Locale.forLanguageTag("es-CO");

    @ModelAttribute("usuarioActual")
    public Map<String, Object> usuarioActual(HttpSession session) {
        Object u = session.getAttribute("usuarioActual");
        if (u instanceof Map<?, ?> map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> typed = (Map<String, Object>) map;
            return typed;
        }
        return null;
    }

    @ModelAttribute("carritoCantidad")
    public int carritoCantidad(HttpSession session) {
        return obtenerCarrito(session).stream().mapToInt(ItemCarrito::getCantidad).sum();
    }

    @ModelAttribute("currencyHelper")
    public CurrencyHelper currencyHelper() {
        return new CurrencyHelper();
    }

    @SuppressWarnings("unchecked")
    protected List<ItemCarrito> obtenerCarrito(HttpSession session) {
        Object raw = session.getAttribute("carrito");
        if (raw instanceof List<?> lista) {
            return (List<ItemCarrito>) lista;
        }
        List<ItemCarrito> nuevo = new ArrayList<>();
        session.setAttribute("carrito", nuevo);
        return nuevo;
    }

    protected ResponseEntity<Map<String, Object>> ok(Map<String, Object> data) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("ok", true);
        body.putAll(data);
        return ResponseEntity.ok(body);
    }

    protected ResponseEntity<Map<String, Object>> fail(String mensaje, int status) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("ok", false);
        body.put("mensaje", mensaje);
        return ResponseEntity.status(status).body(body);
    }

    public static class CurrencyHelper {
        public String cop(long valor) {
            NumberFormat nf = NumberFormat.getCurrencyInstance(LOCALE_CO);
            nf.setMaximumFractionDigits(0);
            return nf.format(valor);
        }
    }
}
