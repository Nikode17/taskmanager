package com.nicolas.taskmanager.security;

import com.nicolas.taskmanager.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. Obtener el header Authorization
        String authHeader = request.getHeader("Authorization");

        String token = null;
        String email = null;

        // 2. Verificar si el header existe y empieza con "Bearer "
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);  // Quitamos "Bearer " (7 caracteres)
            try {
                email = jwtUtil.extractEmail(token);
            } catch (Exception e) {
                // Token inválido o expirado
            }
        }

        // 3. Si el token es válido y no hay autenticación previa
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            if (jwtUtil.validateToken(token, email)) {
                // Token válido, autenticar al usuario
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(email, null, new ArrayList<>());

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Guardamos la autenticación en el contexto de seguridad
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 4. Continuar con la siguiente parte de la petición
        filterChain.doFilter(request, response);
    }
}