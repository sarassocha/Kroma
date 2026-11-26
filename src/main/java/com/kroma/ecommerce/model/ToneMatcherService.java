package com.kroma.ecommerce.model;

import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

/**
 * Servicio POO ToneMatcherService (RF03).
 * Determina el subtono de piel del usuario y ordena los productos Kroma más compatibles.
 */
@Service
public class ToneMatcherService {

    public Diagnostico diagnosticarSubtono(String colorVenas, String reaccionSol, String joyeria) {
        int puntajeFrio = 0;
        int puntajeCalido = 0;

        if ("azuladas".equalsIgnoreCase(colorVenas)) puntajeFrio += 2;
        else if ("verdosas".equalsIgnoreCase(colorVenas)) puntajeCalido += 2;
        else { puntajeFrio += 1; puntajeCalido += 1; }

        if ("se_quema".equalsIgnoreCase(reaccionSol)) puntajeFrio += 2;
        else if ("se_broncea".equalsIgnoreCase(reaccionSol)) puntajeCalido += 2;
        else { puntajeFrio += 1; puntajeCalido += 1; }

        if ("plateada".equalsIgnoreCase(joyeria)) puntajeFrio += 1;
        else if ("dorada".equalsIgnoreCase(joyeria)) puntajeCalido += 1;

        if (puntajeFrio - puntajeCalido >= 2) {
            return new Diagnostico(
                    "Frío",
                    "Tu piel posee matices rosados o azulados; lucen espectaculares los tonos porcelana rosa, malva y berry."
            );
        }
        if (puntajeCalido - puntajeFrio >= 2) {
            return new Diagnostico(
                    "Cálido",
                    "Tu piel brilla con matices dorados, oliva o miel; te favorecen los tonos arena cálida, terracota y coral."
            );
        }
        return new Diagnostico(
                "Neutro",
                "Tu piel equilibra matices rosados y dorados, ideal para tonos beige neutros, arena y durazno suave."
        );
    }

    public List<Recomendacion> recomendarProductos(List<Producto> productos, String subtono, String tipoPiel) {
        return productos.stream()
                .filter(p -> p.isActivo() && p.getStock() > 0)
                .map(p -> {
                    int score = 0;
                    boolean soportaPiel = p.getTipoPiel().contains(tipoPiel) || p.getTipoPiel().contains("Todo tipo");
                    if (soportaPiel) score += 3;
                    if (subtono.equalsIgnoreCase(p.getGamaTono())) score += 4;
                    else if ("Universal".equalsIgnoreCase(p.getGamaTono())) score += 2;

                    Producto.TonoSwatch tono = p.getPaletaTonos().stream()
                            .filter(t -> subtono.equalsIgnoreCase(t.getSubtono()))
                            .findFirst()
                            .orElse(p.getPaletaTonos().isEmpty()
                                    ? new Producto.TonoSwatch("Universal Skin-First", "#E8B4A0", "Universal")
                                    : p.getPaletaTonos().get(0));

                    return new Recomendacion(p, score, tono);
                })
                .filter(r -> r.getScore() >= 3)
                .sorted(Comparator.comparingInt(Recomendacion::getScore).reversed())
                .limit(6)
                .toList();
    }

    public record Diagnostico(String subtono, String explicacion) {
    }

    public static class Recomendacion {
        private final Producto producto;
        private final int score;
        private final Producto.TonoSwatch tonoRecomendado;

        public Recomendacion(Producto producto, int score, Producto.TonoSwatch tonoRecomendado) {
            this.producto = producto;
            this.score = score;
            this.tonoRecomendado = tonoRecomendado;
        }

        public Producto getProducto() { return producto; }
        public int getScore() { return score; }
        public Producto.TonoSwatch getTonoRecomendado() { return tonoRecomendado; }
    }
}
