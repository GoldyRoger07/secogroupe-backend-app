package com.secogroupe.app.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.secogroupe.app.dto.EmployeeRequest;
import com.secogroupe.app.dto.EmployeeResponse;
import com.secogroupe.app.dto.PageResponse;
import com.secogroupe.app.service.EmployeeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final EmployeeService service;

    @PreAuthorize("hasAuthority('READ_EMPLOYEE')")
    @GetMapping
    public ResponseEntity<PageResponse<EmployeeResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String sortField,
            @RequestParam(defaultValue = "asc") String sortOrder,
            @RequestParam(defaultValue = "") String globalFilter) {
        return ResponseEntity.ok(service.getAll(page, size, sortField, sortOrder, globalFilter));
    }

    @PreAuthorize("hasAuthority('CREATE_EMPLOYEE')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EmployeeResponse> create(
            @RequestPart("data") String data,
            @RequestPart(value = "photo", required = false) MultipartFile photo) throws Exception {
        EmployeeRequest request = MAPPER.readValue(data, EmployeeRequest.class);
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request, photo));
    }

    @PreAuthorize("hasAuthority('UPDATE_EMPLOYEE')")
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EmployeeResponse> update(
            @PathVariable Long id,
            @RequestPart("data") String data,
            @RequestPart(value = "photo", required = false) MultipartFile photo) throws Exception {
        EmployeeRequest request = MAPPER.readValue(data, EmployeeRequest.class);
        return ResponseEntity.ok(service.update(id, request, photo));
    }

    @PreAuthorize("hasAuthority('DELETE_EMPLOYEE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
