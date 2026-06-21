package com.secogroupe.app.security;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.MediaType;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.secogroupe.app.service.EndpointPermissionService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * Applique les règles d'autorisation dynamiques (table EndpointPermission)
 * en plus des annotations statiques {@code @PreAuthorize}. Permet de protéger
 * un endpoint avec une permission créée dynamiquement, sans recompilation.
 */
@Component
@RequiredArgsConstructor
public class DynamicAuthorizationFilter extends OncePerRequestFilter {

    private final EndpointPermissionService endpointPermissionService;

    private static final List<String> SKIP_PREFIXES = List.of(
            "/public/", "/api/v1/auth/", "/uploads/", "/error");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        if (isSkipped(path)) {
            chain.doFilter(request, response);
            return;
        }

        List<String> required = endpointPermissionService.requiredPermissions(request.getMethod(), path);
        if (required.isEmpty()) {
            chain.doFilter(request, response);
            return;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean authenticated = auth != null && auth.isAuthenticated()
                && !(auth instanceof AnonymousAuthenticationToken);

        if (!authenticated) {
            writeError(response, 401, "Non authentifié", "Token manquant ou expiré");
            return;
        }

        Set<String> authorities = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        for (String permission : required) {
            if (!authorities.contains(permission)) {
                writeError(response, 403, "Accès refusé",
                        "Permission requise : " + permission);
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private boolean isSkipped(String path) {
        return SKIP_PREFIXES.stream().anyMatch(path::startsWith);
    }

    private void writeError(HttpServletResponse response, int status, String error, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"error\":\"" + error + "\",\"message\":\"" + message + "\"}");
    }
}
