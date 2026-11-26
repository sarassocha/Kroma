package com.kroma.ecommerce.controller;

import com.kroma.ecommerce.model.Administrador;
import com.kroma.ecommerce.model.Cliente;
import com.kroma.ecommerce.model.Usuario;
import com.kroma.ecommerce.model.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Controlador AuthController (Hereda de BaseController).
 * Maneja inicio de sesión (Cliente y Administrador), registro y cierre de sesión (RF01, RNF03).
 */
@Controller
public class AuthController extends BaseController {

    private final UsuarioRepository usuarioRepo;

    public AuthController(UsuarioRepository usuarioRepo) {
        this.usuarioRepo = usuarioRepo;
    }

    @GetMapping("/auth/login")
    public String mostrarLoginCliente(@RequestParam(required = false) String error,
                                      @RequestParam(defaultValue = "/") String next,
                                      HttpSession session,
                                      Model model) {
        Map<String, Object> actual = usuarioActual(session);
        if (actual != null) {
            return "admin".equals(actual.get("rol")) ? "redirect:/admin" : "redirect:/";
        }
        model.addAttribute("titulo", "Iniciar Sesión | Kroma Cosmetics");
        model.addAttribute("modoAdmin", false);
        model.addAttribute("error", error);
        model.addAttribute("nextUrl", next);
        return "auth/login";
    }

    @GetMapping("/auth/admin-login")
    public String mostrarLoginAdmin(@RequestParam(required = false) String error,
                                    HttpSession session,
                                    Model model) {
        Map<String, Object> actual = usuarioActual(session);
        if (actual != null && "admin".equals(actual.get("rol"))) {
            return "redirect:/admin";
        }
        String msgError = "acceso_restringido".equals(error)
                ? "Ingresa con tus credenciales especiales de Administrador Kroma."
                : error;
        model.addAttribute("titulo", "Portal Administrativo | Kroma Cosmetics");
        model.addAttribute("modoAdmin", true);
        model.addAttribute("error", msgError);
        model.addAttribute("nextUrl", "/admin");
        return "auth/login";
    }

    @GetMapping("/auth/registro")
    public String mostrarRegistro(Model model) {
        model.addAttribute("titulo", "Crear Cuenta Skin-First | Kroma Cosmetics");
        model.addAttribute("error", null);
        return "auth/registro";
    }

    @PostMapping("/auth/registro")
    public String registrarCliente(@RequestParam String nombre,
                                   @RequestParam String correo,
                                   @RequestParam String password,
                                   @RequestParam(defaultValue = "") String cedula,
                                   @RequestParam(defaultValue = "") String telefono,
                                   @RequestParam(defaultValue = "") String direccion,
                                   @RequestParam(defaultValue = "Bogotá") String ciudad,
                                   @RequestParam(defaultValue = "Cundinamarca") String departamento,
                                   HttpSession session,
                                   Model model) {
        try {
            if (usuarioRepo.findByCorreoIgnoreCase(correo.trim()).isPresent()) {
                throw new IllegalArgumentException("Ya existe una cuenta registrada con ese correo.");
            }
            Cliente nuevo = new Cliente(nombre, correo, cedula, telefono, direccion, ciudad, departamento);
            nuevo.establecerPassword(password);
            nuevo.validar();
            Cliente guardado = usuarioRepo.save(nuevo);
            guardarSesion(session, guardado);
            return "redirect:/";
        } catch (Exception e) {
            model.addAttribute("titulo", "Crear Cuenta Skin-First | Kroma Cosmetics");
            model.addAttribute("error", e.getMessage());
            return "auth/registro";
        }
    }

    @PostMapping("/auth/login")
    public String iniciarSesionWeb(@RequestParam String correo,
                                   @RequestParam String password,
                                   @RequestParam(defaultValue = "false") boolean modoAdmin,
                                   @RequestParam(defaultValue = "/") String nextUrl,
                                   HttpSession session,
                                   Model model) {
        Optional<Usuario> opt = usuarioRepo.findByCorreoIgnoreCase(correo.trim());
        if (opt.isEmpty() || !opt.get().verificarPassword(password)) {
            model.addAttribute("titulo", modoAdmin ? "Portal Administrativo | Kroma" : "Iniciar Sesión | Kroma");
            model.addAttribute("modoAdmin", modoAdmin);
            model.addAttribute("error", "Correo o contraseña incorrectos.");
            model.addAttribute("nextUrl", nextUrl);
            return "auth/login";
        }

        Usuario usuario = opt.get();
        if (modoAdmin && !(usuario instanceof Administrador)) {
            model.addAttribute("titulo", "Portal Administrativo | Kroma");
            model.addAttribute("modoAdmin", true);
            model.addAttribute("error", "Esta cuenta pertenece a un cliente y no tiene permisos de Administrador.");
            model.addAttribute("nextUrl", "/admin");
            return "auth/login";
        }

        guardarSesion(session, usuario);
        if (usuario instanceof Administrador) {
            return "redirect:/admin";
        }
        return "redirect:" + (nextUrl.startsWith("/") ? nextUrl : "/");
    }

    @PostMapping("/api/auth/login")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> iniciarSesionApi(@RequestBody Map<String, Object> body,
                                                                HttpSession session) {
        String correo = String.valueOf(body.getOrDefault("correo", "")).trim();
        String password = String.valueOf(body.getOrDefault("password", ""));
        boolean modoAdmin = Boolean.parseBoolean(String.valueOf(body.getOrDefault("modoAdmin", "false")));

        Optional<Usuario> opt = usuarioRepo.findByCorreoIgnoreCase(correo);
        if (opt.isEmpty() || !opt.get().verificarPassword(password)) {
            return fail("Credenciales inválidas.", 401);
        }

        Usuario usuario = opt.get();
        if (modoAdmin && !(usuario instanceof Administrador)) {
            return fail("Esta cuenta no tiene privilegios de administrador.", 403);
        }

        Map<String, Object> sesionMap = guardarSesion(session, usuario);
        return ok(Map.of("usuario", sesionMap, "redirect", usuario.getPanelRuta()));
    }

    @GetMapping("/auth/logout")
    public String cerrarSesion(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    private Map<String, Object> guardarSesion(HttpSession session, Usuario usuario) {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("id", usuario.getId());
        info.put("nombre", usuario.getNombre());
        info.put("correo", usuario.getCorreo());
        info.put("rol", usuario.getRol());
        info.put("permisos", usuario.getPermisos());
        session.setAttribute("usuarioActual", info);
        return info;
    }
}
