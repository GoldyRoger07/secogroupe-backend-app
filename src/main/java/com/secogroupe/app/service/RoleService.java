package com.secogroupe.app.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.secogroupe.app.dto.PageResponse;
import com.secogroupe.app.dto.RoleRequest;
import com.secogroupe.app.dto.RoleResponse;
import com.secogroupe.app.entity.Permission;
import com.secogroupe.app.entity.Role;
import com.secogroupe.app.mapper.RoleMapper;
import com.secogroupe.app.repository.PermissionRepository;
import com.secogroupe.app.repository.RoleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RoleMapper roleMapper;
    private final ObjectMapper objectMapper;

    public PageResponse<RoleResponse> getAll(int page, int size, String sortField, String sortOrder, String filter) {
        Sort sort = (sortField != null && !sortField.isBlank())
                ? Sort.by("desc".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC, sortField)
                : Sort.unsorted();
        Pageable pageable = PageRequest.of(page, size, sort);
        String f = (filter != null && !filter.isBlank()) ? filter : "";

        Page<Role> resultPage = f.isEmpty()
                ? roleRepository.findAll(pageable)
                : roleRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(f, f, pageable);

        return new PageResponse<>(
                resultPage.getContent().stream().map(roleMapper::toResponse).toList(),
                resultPage.getTotalElements(),
                resultPage.getTotalPages(),
                page,
                size);
    }

    @Transactional
    public RoleResponse create(RoleRequest request) {
        if (roleRepository.findByName(request.getName()).isPresent()) {
            throw new RuntimeException("Un rôle avec ce nom existe déjà");
        }
        Role role = new Role(request.getName());
        role.setDescription(request.getDescription());
        role.setPermissions(resolvePermissions(request.getPermissionIds()));
        return roleMapper.toResponse(roleRepository.save(role));
    }

    @Transactional
    public RoleResponse update(Long id, RoleRequest request) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rôle introuvable"));

        roleRepository.findByName(request.getName()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new RuntimeException("Un rôle avec ce nom existe déjà");
            }
        });

        role.setName(request.getName());
        role.setDescription(request.getDescription());
        role.setPermissions(resolvePermissions(request.getPermissionIds()));
        return roleMapper.toResponse(roleRepository.save(role));
    }

    @Transactional
    public void delete(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rôle introuvable"));
        role.setPermissions(Set.of());
        roleRepository.save(role);
        roleRepository.delete(role);
    }

    public List<RoleResponse> listAll() {
        return roleRepository.findAll().stream().map(roleMapper::toResponse).toList();
    }

    // ──────────────── Export ────────────────

    public byte[] exportCsv() {
        List<Role> all = roleRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (CSVPrinter printer = new CSVPrinter(
                new OutputStreamWriter(baos, StandardCharsets.UTF_8),
                CSVFormat.DEFAULT.builder()
                        .setHeader("id", "name", "description", "permissions", "createdAt")
                        .build())) {
            for (Role r : all) {
                String perms = r.getPermissions() != null
                        ? r.getPermissions().stream().map(Permission::getName).sorted().collect(Collectors.joining(";"))
                        : "";
                printer.printRecord(
                        r.getId(), r.getName(), r.getDescription(), perms,
                        r.getCreatedAt() != null ? r.getCreatedAt().toString() : "");
            }
        } catch (IOException ex) {
            throw new RuntimeException("Erreur export CSV", ex);
        }
        return baos.toByteArray();
    }

    public byte[] exportJson() {
        List<RoleResponse> all = roleRepository.findAll(Sort.by(Sort.Direction.ASC, "name"))
                .stream().map(roleMapper::toResponse).toList();
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(all);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException("Erreur export JSON", ex);
        }
    }

    // ──────────────── private helpers ────────────────

    private Set<Permission> resolvePermissions(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return Set.of();
        return new HashSet<>(permissionRepository.findAllById(ids));
    }
}
