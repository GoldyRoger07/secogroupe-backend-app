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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.secogroupe.app.dto.ImportResult;
import com.secogroupe.app.dto.PageResponse;
import com.secogroupe.app.dto.UserRequest;
import com.secogroupe.app.dto.UserResponse;
import com.secogroupe.app.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PreAuthorize("hasAuthority('READ_USER')")
    @GetMapping
    public ResponseEntity<PageResponse<UserResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String sortField,
            @RequestParam(defaultValue = "asc") String sortOrder,
            @RequestParam(defaultValue = "") String globalFilter) {
        return ResponseEntity.ok(userService.getAllUsers(page, size, sortField, sortOrder, globalFilter));
    }

    @PreAuthorize("hasAuthority('CREATE_USER')")
    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(request));
    }

    @PreAuthorize("hasAuthority('UPDATE_USER')")
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(@PathVariable Long id,
                                               @Valid @RequestBody UserRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @PreAuthorize("hasAuthority('DELETE_USER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // ──────────────── Export ────────────────

    @PreAuthorize("hasAuthority('READ_USER')")
    @GetMapping("/export/csv")
    public ResponseEntity<byte[]> exportCsv() {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"users.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(userService.exportCsv());
    }

    @PreAuthorize("hasAuthority('READ_USER')")
    @GetMapping("/export/json")
    public ResponseEntity<byte[]> exportJson() {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"users.json\"")
                .contentType(MediaType.APPLICATION_JSON)
                .body(userService.exportJson());
    }

    // ──────────────── Import ────────────────

    @PreAuthorize("hasAuthority('CREATE_USER')")
    @PostMapping("/import/csv")
    public ResponseEntity<ImportResult> importCsv(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(userService.importCsv(file));
    }

    @PreAuthorize("hasAuthority('CREATE_USER')")
    @PostMapping("/import/json")
    public ResponseEntity<ImportResult> importJson(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(userService.importJson(file));
    }
}
