package com.secogroupe.app.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.secogroupe.app.dto.CongeRequest;
import com.secogroupe.app.dto.CongeResponse;
import com.secogroupe.app.dto.PageResponse;
import com.secogroupe.app.model.CongeStatus;
import com.secogroupe.app.service.CongeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/conges")
@RequiredArgsConstructor
public class CongeController {

    private final CongeService service;

    @PreAuthorize("hasAuthority('READ_EMPLOYEE')")
    @GetMapping
    public ResponseEntity<PageResponse<CongeResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String sortField,
            @RequestParam(defaultValue = "desc") String sortOrder,
            @RequestParam(defaultValue = "") String globalFilter) {
        return ResponseEntity.ok(service.getAll(page, size, sortField, sortOrder, globalFilter));
    }

    @PreAuthorize("hasAuthority('READ_EMPLOYEE')")
    @GetMapping("/{id}")
    public ResponseEntity<CongeResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PreAuthorize("hasAuthority('CREATE_EMPLOYEE')")
    @PostMapping
    public ResponseEntity<CongeResponse> create(@Valid @RequestBody CongeRequest request) {
        return ResponseEntity.ok(service.create(request));
    }

    @PreAuthorize("hasAuthority('UPDATE_EMPLOYEE')")
    @PutMapping("/{id}")
    public ResponseEntity<CongeResponse> update(@PathVariable Long id, @Valid @RequestBody CongeRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @PreAuthorize("hasAuthority('UPDATE_EMPLOYEE')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<CongeResponse> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        CongeStatus status = CongeStatus.valueOf(body.get("status"));
        return ResponseEntity.ok(service.updateStatus(id, status, body.get("managerComment")));
    }

    @PreAuthorize("hasAuthority('DELETE_EMPLOYEE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
