package com.kroma.ecommerce;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 4.2 Pruebas de Integración MVC + MongoDB + Seguridad Admin (Java Spring Boot).
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "kroma.mongodb.force-embedded=true",
        "kroma.mongodb.embedded-fallback=true"
})
class MvcIntegracionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void vistaClienteCargaCatalogoYFiltrosSkinFirst() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("KROMA")))
                .andExpect(content().string(containsString("Base Sérum Skin-First Luminous")));

        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.total").value(greaterThanOrEqualTo(10)));

        mockMvc.perform(get("/api/productos?categoria=Labios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(greaterThanOrEqualTo(2)));
    }

    @Test
    void toneMatcherApiCalculaDiagnostico() throws Exception {
        String payload = """
                {
                  "colorVenas": "azuladas",
                  "reaccionSol": "se_quema",
                  "joyeria": "plateada",
                  "tipoPiel": "Seca"
                }
                """;
        mockMvc.perform(post("/api/tone-matcher")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.diagnostico.subtono").value("Frío"))
                .andExpect(jsonPath("$.recomendaciones", not(empty())));
    }

    @Test
    void seguridadAdminYCrudCompletoConHistorial() throws Exception {
        // 1. Acceso sin sesión a API Admin debe retornar 403
        mockMvc.perform(get("/admin/api/pedidos"))
                .andExpect(status().isForbidden());

        // 2. Cliente normal no puede acceder a /admin/api/pedidos
        MvcResult loginClienteRes = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"camila@ejemplo.com.co\",\"password\":\"Cliente123!\"}"))
                .andExpect(status().isOk())
                .andReturn();
        MockHttpSession clienteSession = (MockHttpSession) loginClienteRes.getRequest().getSession(false);

        mockMvc.perform(get("/admin/api/pedidos").session(clienteSession))
                .andExpect(status().isForbidden());

        // 3. Login con credenciales especiales de Administrador
        MvcResult loginAdminRes = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"admin@kroma.com.co\",\"password\":\"KromaAdmin2026!\",\"modoAdmin\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario.rol").value("admin"))
                .andReturn();
        MockHttpSession adminSession = (MockHttpSession) loginAdminRes.getRequest().getSession(false);

        // 3.5 Verificar que las vistas HTML clave (Admin, Mis Pedidos, Checkout, Login, Tone Matcher) renderizan sin errores Thymeleaf
        mockMvc.perform(get("/tone-matcher")).andExpect(status().isOk());
        mockMvc.perform(get("/checkout")).andExpect(status().isOk());
        mockMvc.perform(get("/auth/login")).andExpect(status().isOk());
        mockMvc.perform(get("/auth/admin-login")).andExpect(status().isOk());
        mockMvc.perform(get("/mis-pedidos").session(clienteSession))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("KRM-104820")));
        mockMvc.perform(get("/admin").session(adminSession))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Gestión de Catálogo y Ventas Kroma")));

        // 4. Ver historial de compras
        mockMvc.perform(get("/admin/api/pedidos").session(adminSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(greaterThanOrEqualTo(1)));

        // 5. Agregar producto
        String nuevoProducto = """
                {
                  "codigo": "KRM-TEST-JAVA",
                  "nombre": "Iluminador Líquido Sol Colombiano",
                  "descripcionCorta": "Brillo sutil hidratante sin glitter grueso.",
                  "descripcionIngredientes": "Escualano vegetal, Vitamina E, Mica ética no abrasiva. Cruelty-Free.",
                  "categoria": "Rostro",
                  "precio": 41000,
                  "stock": 25,
                  "gamaTono": "Cálido",
                  "tipoPiel": ["Seca", "Mixta"],
                  "paletaTonos": "Champagne|#F4DFC8|Cálido"
                }
                """;
        MvcResult creadoRes = mockMvc.perform(post("/admin/api/productos")
                        .session(adminSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(nuevoProducto))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.producto.nombre").value("Iluminador Líquido Sol Colombiano"))
                .andReturn();

        JsonNode creadoJson = objectMapper.readTree(creadoRes.getResponse().getContentAsString());
        String prodId = creadoJson.path("producto").path("id").asText();

        // 6. Editar especificaciones del producto
        mockMvc.perform(put("/admin/api/productos/" + prodId)
                        .session(adminSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"precio\": 45900, \"stock\": 30}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.producto.precio").value(45900))
                .andExpect(jsonPath("$.producto.stock").value(30));

        // 7. Eliminar producto
        mockMvc.perform(delete("/admin/api/productos/" + prodId).session(adminSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true));
    }
}
