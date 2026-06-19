package com.secogroupe.app.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.secogroupe.app.dto.ImportResult;
import com.secogroupe.app.dto.PageResponse;
import com.secogroupe.app.dto.PermissionRequest;
import com.secogroupe.app.dto.PermissionResponse;
import com.secogroupe.app.entity.Permission;
import com.secogroupe.app.mapper.PermissionMapper;
import com.secogroupe.app.repository.PermissionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;
    private final ObjectMapper objectMapper;

    public PageResponse<PermissionResponse> getAll(int page, int size, String sortField, String sortOrder, String filter) {
        Sort sort = (sortField != null && !sortField.isBlank())
                ? Sort.by("desc".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC, sortField)
                : Sort.unsorted();
        Pageable pageable = PageRequest.of(page, size, sort);
        String f = (filter != null && !filter.isBlank()) ? filter : "";

        Page<Permission> resultPage = f.isEmpty()
                ? permissionRepository.findAll(pageable)
                : permissionRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrModuleContainingIgnoreCase(f, f, f, pageable);

        return new PageResponse<>(
                resultPage.getContent().stream().map(permissionMapper::toResponse).toList(),
                resultPage.getTotalElements(),
                resultPage.getTotalPages(),
                page,
                size);
    }

    public List<PermissionResponse> listAll() {
        return permissionRepository.findAll().stream().map(permissionMapper::toResponse).toList();
    }

    @Transactional
    public PermissionResponse create(PermissionRequest request) {
        if (permissionRepository.findByName(request.getName()).isPresent()) {
            throw new RuntimeException("Une permission avec ce nom existe déjà");
        }
        Permission permission = new Permission(request.getName());
        permission.setDescription(request.getDescription());
        permission.setModule(request.getModule());
        permission.setAction(request.getAction());
        return permissionMapper.toResponse(permissionRepository.save(permission));
    }

    @Transactional
    public PermissionResponse update(Long id, PermissionRequest request) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Permission introuvable"));

        permissionRepository.findByName(request.getName()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new RuntimeException("Une permission avec ce nom existe déjà");
            }
        });

        permission.setName(request.getName());
        permission.setDescription(request.getDescription());
        permission.setModule(request.getModule());
        permission.setAction(request.getAction());
        return permissionMapper.toResponse(permissionRepository.save(permission));
    }

    @Transactional
    public void delete(Long id) {
        if (!permissionRepository.existsById(id)) {
            throw new RuntimeException("Permission introuvable");
        }
        permissionRepository.deleteById(id);
    }

    // ──────────────── Export ────────────────

    public byte[] exportCsv() {
        List<Permission> all = permissionRepository.findAll(Sort.by(Sort.Direction.ASC, "module", "name"));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (CSVPrinter printer = new CSVPrinter(
                new OutputStreamWriter(baos, StandardCharsets.UTF_8),
                CSVFormat.DEFAULT.builder()
                        .setHeader("id", "name", "description", "module", "action", "createdAt")
                        .build())) {
            for (Permission p : all) {
                printer.printRecord(
                        p.getId(), p.getName(), p.getDescription(), p.getModule(),
                        p.getAction(), p.getCreatedAt() != null ? p.getCreatedAt().toString() : "");
            }
        } catch (IOException ex) {
            throw new RuntimeException("Erreur export CSV", ex);
        }
        return baos.toByteArray();
    }

    public byte[] exportJson() {
        List<PermissionResponse> all = permissionRepository.findAll(Sort.by(Sort.Direction.ASC, "module", "name"))
                .stream().map(permissionMapper::toResponse).toList();
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(all);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException("Erreur export JSON", ex);
        }
    }

    // ──────────────── Import ────────────────

    @Transactional
    public ImportResult importCsv(MultipartFile file) {
        int imported = 0, skipped = 0;
        List<String> errors = new ArrayList<>();
        try (CSVParser parser = CSVParser.parse(file.getInputStream(), StandardCharsets.UTF_8,
                CSVFormat.DEFAULT.builder()
                        .setHeader().setSkipHeaderRecord(true)
                        .setIgnoreHeaderCase(true).setTrim(true).build())) {
            for (CSVRecord record : parser) {
                try {
                    String name = record.get("name");
                    if (permissionRepository.findByName(name).isPresent()) {
                        skipped++;
                        continue;
                    }
                    Permission p = new Permission(name);
                    p.setDescription(safeGet(record, "description"));
                    p.setModule(safeGet(record, "module"));
                    p.setAction(safeGet(record, "action"));
                    permissionRepository.save(p);
                    imported++;
                } catch (Exception e) {
                    errors.add("Ligne " + record.getRecordNumber() + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Lecture du fichier CSV impossible", e);
        }
        return new ImportResult(imported, skipped, errors);
    }

    @Transactional
    public ImportResult importJson(MultipartFile file) {
        int imported = 0, skipped = 0;
        List<String> errors = new ArrayList<>();
        try {
            List<PermissionRequest> requests = objectMapper.readValue(file.getInputStream(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, PermissionRequest.class));
            int idx = 1;
            for (PermissionRequest req : requests) {
                try {
                    if (permissionRepository.findByName(req.getName()).isPresent()) {
                        skipped++;
                    } else {
                        Permission p = new Permission(req.getName());
                        p.setDescription(req.getDescription());
                        p.setModule(req.getModule());
                        p.setAction(req.getAction());
                        permissionRepository.save(p);
                        imported++;
                    }
                } catch (Exception e) {
                    errors.add("Entrée " + idx + ": " + e.getMessage());
                }
                idx++;
            }
        } catch (IOException e) {
            throw new RuntimeException("Lecture du fichier JSON impossible", e);
        }
        return new ImportResult(imported, skipped, errors);
    }

    // ──────────────── private helpers ────────────────

    private static String safeGet(CSVRecord r, String col) {
        try { return r.get(col); } catch (IllegalArgumentException e) { return null; }
    }
}
