package com.secogroupe.app.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.secogroupe.app.dto.ApplicationRequestDto;
import com.secogroupe.app.dto.ApplicationResponse;
import com.secogroupe.app.dto.ApplicationUpdateRequest;
import com.secogroupe.app.dto.PageResponse;
import com.secogroupe.app.service.ApplicationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    // ──────────────── Public submission ────────────────

    @PostMapping("/public/v1/applications")
    public ResponseEntity<String> submit(@Valid @RequestBody ApplicationRequestDto dto) {
        applicationService.submit(dto);
        return ResponseEntity.ok("""
                {
                    "message":"Thank you! Your application has been received. Our recruitment team will get back to you shortly."
                }
                """);
    }

    // ──────────────── Management (admin) ────────────────

    @PreAuthorize("hasAuthority('READ_APPLICATION')")
    @GetMapping("/api/v1/applications")
    public ResponseEntity<PageResponse<ApplicationResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String sortField,
            @RequestParam(defaultValue = "desc") String sortOrder,
            @RequestParam(defaultValue = "") String globalFilter) {
        return ResponseEntity.ok(applicationService.getAll(page, size, sortField, sortOrder, globalFilter));
    }

    @PreAuthorize("hasAuthority('READ_APPLICATION')")
    @GetMapping("/api/v1/applications/{id}")
    public ResponseEntity<ApplicationResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(applicationService.getById(id));
    }

    @PreAuthorize("hasAuthority('UPDATE_APPLICATION')")
    @PutMapping("/api/v1/applications/{id}")
    public ResponseEntity<ApplicationResponse> update(@PathVariable Long id,
                                                      @Valid @RequestBody ApplicationUpdateRequest request) {
        return ResponseEntity.ok(applicationService.update(id, request));
    }

    @PreAuthorize("hasAuthority('DELETE_APPLICATION')")
    @DeleteMapping("/api/v1/applications/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        applicationService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // ──────────────── Export ────────────────

    @PreAuthorize("hasAuthority('READ_APPLICATION')")
    @GetMapping("/api/v1/applications/export/csv")
    public ResponseEntity<byte[]> exportCsv() {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"applications.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(applicationService.exportCsv());
    }

    @PreAuthorize("hasAuthority('READ_APPLICATION')")
    @GetMapping("/api/v1/applications/export/json")
    public ResponseEntity<byte[]> exportJson() {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"applications.json\"")
                .contentType(MediaType.APPLICATION_JSON)
                .body(applicationService.exportJson());
    }
}
