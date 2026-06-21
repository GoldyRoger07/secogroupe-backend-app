package com.secogroupe.app.controller;

import java.security.Principal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.secogroupe.app.dto.AttendanceCodeResponse;
import com.secogroupe.app.dto.AttendanceResponse;
import com.secogroupe.app.dto.PageResponse;
import com.secogroupe.app.dto.ScanRequest;
import com.secogroupe.app.dto.ScanResponse;
import com.secogroupe.app.service.AttendanceService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    // ──────────────── Code rotatif affiché par l'admin ────────────────

    @PreAuthorize("hasAuthority('READ_ATTENDANCE')")
    @GetMapping("/code")
    public ResponseEntity<AttendanceCodeResponse> currentCode() {
        return ResponseEntity.ok(attendanceService.currentCode());
    }

    // ──────────────── Scan par l'employé connecté ────────────────

    @PostMapping("/scan")
    public ResponseEntity<ScanResponse> scan(@Valid @RequestBody ScanRequest request, Principal principal) {
        return ResponseEntity.ok(attendanceService.scan(request.getCode(), principal.getName()));
    }

    // ──────────────── Mon historique (employé connecté) ────────────────

    @GetMapping("/me")
    public ResponseEntity<PageResponse<AttendanceResponse>> myAttendance(
            Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String sortField,
            @RequestParam(defaultValue = "desc") String sortOrder) {
        return ResponseEntity.ok(attendanceService.getMine(principal.getName(), page, size, sortField, sortOrder));
    }

    // ──────────────── Consultation (admin) ────────────────

    @PreAuthorize("hasAuthority('READ_ATTENDANCE')")
    @GetMapping
    public ResponseEntity<PageResponse<AttendanceResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String sortField,
            @RequestParam(defaultValue = "desc") String sortOrder,
            @RequestParam(defaultValue = "") String globalFilter) {
        return ResponseEntity.ok(attendanceService.getAll(page, size, sortField, sortOrder, globalFilter));
    }

    @PreAuthorize("hasAuthority('DELETE_ATTENDANCE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        attendanceService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
