package com.secogroupe.app.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.secogroupe.app.dto.MetadataRequest;
import com.secogroupe.app.service.MetadataService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class MetadataController {

    private final MetadataService service;

    // ──────────────── Departments ────────────────

    @PreAuthorize("hasAuthority('READ_EMPLOYEE')")
    @GetMapping("/departments")
    public ResponseEntity<List<String>> getDepartments() {
        return ResponseEntity.ok(service.getDepartments());
    }

    @PreAuthorize("hasAnyAuthority('CREATE_EMPLOYEE', 'UPDATE_EMPLOYEE')")
    @PostMapping("/departments")
    public ResponseEntity<String> createDepartment(@Valid @RequestBody MetadataRequest request) {
        return ResponseEntity.ok(service.createDepartment(request.getName()));
    }

    // ──────────────── Positions ────────────────

    @PreAuthorize("hasAuthority('READ_EMPLOYEE')")
    @GetMapping("/positions")
    public ResponseEntity<List<String>> getPositions() {
        return ResponseEntity.ok(service.getPositions());
    }

    @PreAuthorize("hasAnyAuthority('CREATE_EMPLOYEE', 'UPDATE_EMPLOYEE')")
    @PostMapping("/positions")
    public ResponseEntity<String> createPosition(@Valid @RequestBody MetadataRequest request) {
        return ResponseEntity.ok(service.createPosition(request.getName()));
    }

    // ──────────────── Banques ────────────────

    @PreAuthorize("hasAuthority('READ_EMPLOYEE')")
    @GetMapping("/banques")
    public ResponseEntity<List<String>> getBanques() {
        return ResponseEntity.ok(service.getBanques());
    }

    @PreAuthorize("hasAnyAuthority('CREATE_EMPLOYEE', 'UPDATE_EMPLOYEE')")
    @PostMapping("/banques")
    public ResponseEntity<String> createBanque(@Valid @RequestBody MetadataRequest request) {
        return ResponseEntity.ok(service.createBanque(request.getName()));
    }

    // ──────────────── États civils ────────────────

    @PreAuthorize("hasAuthority('READ_EMPLOYEE')")
    @GetMapping("/etats-civils")
    public ResponseEntity<List<String>> getEtatsCivils() {
        return ResponseEntity.ok(service.getEtatsCivils());
    }

    @PreAuthorize("hasAnyAuthority('CREATE_EMPLOYEE', 'UPDATE_EMPLOYEE')")
    @PostMapping("/etats-civils")
    public ResponseEntity<String> createEtatCivil(@Valid @RequestBody MetadataRequest request) {
        return ResponseEntity.ok(service.createEtatCivil(request.getName()));
    }

    // ──────────────── Types de congé ────────────────

    @PreAuthorize("hasAuthority('READ_EMPLOYEE')")
    @GetMapping("/types-conges")
    public ResponseEntity<List<String>> getTypesConges() {
        return ResponseEntity.ok(service.getTypesConges());
    }

    @PreAuthorize("hasAnyAuthority('CREATE_EMPLOYEE', 'UPDATE_EMPLOYEE')")
    @PostMapping("/types-conges")
    public ResponseEntity<String> createTypeConge(@Valid @RequestBody MetadataRequest request) {
        return ResponseEntity.ok(service.createTypeConge(request.getName()));
    }
}
