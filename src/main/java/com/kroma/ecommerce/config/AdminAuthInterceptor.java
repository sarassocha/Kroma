package com.kroma.ecommerce.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;

/**
 * Interceptor de seguridad para proteger todas las rutas /admin/**.
 * Solo permite el paso a usuarios autenticados con rol "admin".
 */
@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        Object usuarioObj = session != null ? session.getAttribute("usuarioActual") : null;

        if (usuarioObj instanceof Map<?, ?> usuario && "admin".equals(usuario.get("rol"))) {
            return true;
        }

        if (request.getRequestURI().startsWith("/admin/api/")) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"ok\":false,\"mensaje\":\"Acceso restringido. Se requieren credenciales especiales de Administrador.\"}");
            return false;
        }

        response.sendRedirect("/auth/admin-login?error=acceso_restringido");
        return false;
    }
}
