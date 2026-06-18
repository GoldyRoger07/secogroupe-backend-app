package com.secogroupe.app.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.secogroupe.app.dto.PageResponse;
import com.secogroupe.app.dto.PermissionRequest;
import com.secogroupe.app.dto.PermissionResponse;
import com.secogroupe.app.service.PermissionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @PreAuthorize("hasAuthority('READ_PERMISSION')")
    @GetMapping
    public ResponseEntity<PageResponse<PermissionResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String sortField,
            @RequestParam(defaultValue = "asc") String sortOrder,
            @RequestParam(defaultValue = "") String globalFilter) {
        return ResponseEntity.ok(permissionService.getAll(page, size, sortField, sortOrder, globalFilter));
    }

    @PreAuthorize("hasAuthority('READ_PERMISSION')")
    @GetMapping("/list")
    public ResponseEntity<List<PermissionResponse>> listAll() {
        return ResponseEntity.ok(permissionService.listAll());
    }

    @PreAuthorize("hasAuthority('CREATE_PERMISSION')")
    @PostMapping
    public ResponseEntity<PermissionResponse> create(@Valid @RequestBody PermissionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(permissionService.create(request));
    }

    @PreAuthorize("hasAuthority('UPDATE_PERMISSION')")
    @PutMapping("/{id}")
    public ResponseEntity<PermissionResponse> update(@PathVariable Long id,
                                                     @Valid @RequestBody PermissionRequest request) {
        return ResponseEntity.ok(permissionService.update(id, request));
    }

    @PreAuthorize("hasAuthority('DELETE_PERMISSION')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        permissionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
