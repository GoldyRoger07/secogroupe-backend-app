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

import com.secogroupe.app.dto.PageResponse;
import com.secogroupe.app.dto.QuoteRequestDto;
import com.secogroupe.app.dto.QuoteResponse;
import com.secogroupe.app.dto.QuoteUpdateRequest;
import com.secogroupe.app.service.QuoteService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class QuoteController {

    private final QuoteService quoteService;

    // ──────────────── Public submission ────────────────
// @Valid
    @PostMapping("/public/v1/quotes")
    public ResponseEntity<String> submit(@Valid @RequestBody QuoteRequestDto dto) {
        System.out.println("Hello World");
        quoteService.submit(dto);
        return ResponseEntity.ok("Your quote request has been received. We will contact you shortly.");
    }

    // ──────────────── Management (admin) ────────────────

    @PreAuthorize("hasAuthority('READ_QUOTE')")
    @GetMapping("/api/v1/quotes")
    public ResponseEntity<PageResponse<QuoteResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String sortField,
            @RequestParam(defaultValue = "desc") String sortOrder,
            @RequestParam(defaultValue = "") String globalFilter) {
        return ResponseEntity.ok(quoteService.getAll(page, size, sortField, sortOrder, globalFilter));
    }

    @PreAuthorize("hasAuthority('READ_QUOTE')")
    @GetMapping("/api/v1/quotes/{id}")
    public ResponseEntity<QuoteResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(quoteService.getById(id));
    }

    @PreAuthorize("hasAuthority('UPDATE_QUOTE')")
    @PutMapping("/api/v1/quotes/{id}")
    public ResponseEntity<QuoteResponse> update(@PathVariable Long id,
                                                @Valid @RequestBody QuoteUpdateRequest request) {
        return ResponseEntity.ok(quoteService.update(id, request));
    }

    @PreAuthorize("hasAuthority('DELETE_QUOTE')")
    @DeleteMapping("/api/v1/quotes/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        quoteService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // ──────────────── Export ────────────────

    @PreAuthorize("hasAuthority('READ_QUOTE')")
    @GetMapping("/api/v1/quotes/export/csv")
    public ResponseEntity<byte[]> exportCsv() {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"quotes.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(quoteService.exportCsv());
    }

    @PreAuthorize("hasAuthority('READ_QUOTE')")
    @GetMapping("/api/v1/quotes/export/json")
    public ResponseEntity<byte[]> exportJson() {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"quotes.json\"")
                .contentType(MediaType.APPLICATION_JSON)
                .body(quoteService.exportJson());
    }
}
