package com.secogroupe.app.service;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.AntPathMatcher;

import com.secogroupe.app.dto.EndpointPermissionRequest;
import com.secogroupe.app.dto.EndpointPermissionResponse;
import com.secogroupe.app.dto.PageResponse;
import com.secogroupe.app.entity.EndpointPermission;
import com.secogroupe.app.exception.ResourceNotFoundException;
import com.secogroupe.app.repository.EndpointPermissionRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EndpointPermissionService {

    private final EndpointPermissionRepository repository;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /** Cache des règles actives, rechargé à chaque modification (évite une requête DB par requête HTTP). */
    private final AtomicReference<List<EndpointPermission>> cache = new AtomicReference<>(List.of());

    @PostConstruct
    public void reload() {
        cache.set(repository.findByEnabledTrue());
    }

    // ──────────────── Évaluation (filtre de sécurité) ────────────────

    /** Noms des permissions requises pour une requête (méthode + chemin). */
    public List<String> requiredPermissions(String method, String path) {
        return cache.get().stream()
                .filter(rule -> methodMatches(rule.getHttpMethod(), method))
                .filter(rule -> pathMatcher.match(rule.getPathPattern(), path))
                .map(EndpointPermission::getPermissionName)
                .distinct()
                .toList();
    }

    private boolean methodMatches(String ruleMethod, String requestMethod) {
        return ruleMethod == null || "*".equals(ruleMethod) || ruleMethod.equalsIgnoreCase(requestMethod);
    }

    // ──────────────── CRUD (admin) ────────────────

    public PageResponse<EndpointPermissionResponse> getAll(int page, int size, String sortField, String sortOrder, String filter) {
        String field = (sortField != null && !sortField.isBlank()) ? sortField : "pathPattern";
        Sort sort = Sort.by("desc".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC, field);
        Pageable pageable = PageRequest.of(page, size, sort);
        String f = (filter != null && !filter.isBlank()) ? filter : "";

        Page<EndpointPermission> result = f.isEmpty()
                ? repository.findAll(pageable)
                : repository.search(f, pageable);

        return new PageResponse<>(
                result.getContent().stream().map(this::toResponse).toList(),
                result.getTotalElements(),
                result.getTotalPages(),
                page,
                size);
    }

    @Transactional
    public EndpointPermissionResponse create(EndpointPermissionRequest request) {
        EndpointPermission entity = new EndpointPermission();
        apply(entity, request);
        EndpointPermissionResponse res = toResponse(repository.save(entity));
        reload();
        return res;
    }

    @Transactional
    public EndpointPermissionResponse update(Long id, EndpointPermissionRequest request) {
        EndpointPermission entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Règle d'endpoint introuvable"));
        apply(entity, request);
        EndpointPermissionResponse res = toResponse(repository.save(entity));
        reload();
        return res;
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Règle d'endpoint introuvable");
        }
        repository.deleteById(id);
        reload();
    }

    // ──────────────── helpers ────────────────

    private void apply(EndpointPermission entity, EndpointPermissionRequest request) {
        entity.setHttpMethod(request.getHttpMethod() == null ? "*" : request.getHttpMethod().trim().toUpperCase());
        entity.setPathPattern(request.getPathPattern().trim());
        entity.setPermissionName(request.getPermissionName().trim());
        entity.setDescription(request.getDescription());
        entity.setEnabled(request.isEnabled());
    }

    private EndpointPermissionResponse toResponse(EndpointPermission e) {
        EndpointPermissionResponse r = new EndpointPermissionResponse();
        r.setId(e.getId());
        r.setHttpMethod(e.getHttpMethod());
        r.setPathPattern(e.getPathPattern());
        r.setPermissionName(e.getPermissionName());
        r.setDescription(e.getDescription());
        r.setEnabled(e.isEnabled());
        r.setCreatedAt(e.getCreatedAt() != null ? e.getCreatedAt().toString() : null);
        return r;
    }
}
