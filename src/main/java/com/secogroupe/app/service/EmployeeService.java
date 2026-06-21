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
import com.secogroupe.app.dto.EmployeeRequest;
import com.secogroupe.app.dto.EmployeeResponse;
import com.secogroupe.app.dto.ImportResult;
import com.secogroupe.app.dto.PageResponse;
import com.secogroupe.app.entity.Employee;
import com.secogroupe.app.entity.EmployeeStatus;
import com.secogroupe.app.entity.User;
import com.secogroupe.app.mapper.EmployeeMapper;
import com.secogroupe.app.repository.EmployeeRepository;
import com.secogroupe.app.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository repository;
    private final EmployeeMapper mapper;
    private final PhotoStorageService photoStorageService;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;

    public PageResponse<EmployeeResponse> getAll(int page, int size, String sortField, String sortOrder, String filter) {
        Sort sort = buildSort(sortField, sortOrder);
        Pageable pageable = PageRequest.of(page, size, sort);
        String f = (filter != null && !filter.isBlank()) ? filter : "";
        Page<Employee> resultPage;
        if (f.isEmpty()) {
            resultPage = repository.findAll(pageable);
        } else {
            resultPage = repository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrPositionContainingIgnoreCaseOrDepartmentContainingIgnoreCase(
                    f, f, f, f, f, pageable);
        }
        return new PageResponse<>(
                resultPage.getContent().stream().map(mapper::toResponse).toList(),
                resultPage.getTotalElements(),
                resultPage.getTotalPages(),
                page,
                size);
    }

    public EmployeeResponse create(EmployeeRequest request, MultipartFile photo) {
        Employee emp = mapper.toEntity(request);
        applyUserLink(emp, request.getUserId());
        if (photo != null && !photo.isEmpty()) {
            emp.setPhotoUrl(photoStorageService.store(photo));
        }
        return mapper.toResponse(repository.save(emp));
    }

    public EmployeeResponse update(Long id, EmployeeRequest request, MultipartFile photo) {
        Employee emp = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employé introuvable"));
        mapper.update(emp, request);
        applyUserLink(emp, request.getUserId());
        if (photo != null && !photo.isEmpty()) {
            if (emp.getPhotoUrl() != null) {
                photoStorageService.delete(emp.getPhotoUrl());
            }
            emp.setPhotoUrl(photoStorageService.store(photo));
        }
        return mapper.toResponse(repository.save(emp));
    }

    public void delete(Long id) {
        Employee emp = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employé introuvable"));
        if (emp.getPhotoUrl() != null) {
            photoStorageService.delete(emp.getPhotoUrl());
        }
        repository.delete(emp);
    }

    // ──────────────── Export ────────────────

    public byte[] exportCsv() {
        List<Employee> all = repository.findAll(Sort.by(Sort.Direction.ASC, "lastName"));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (CSVPrinter printer = new CSVPrinter(
                new OutputStreamWriter(baos, StandardCharsets.UTF_8),
                CSVFormat.DEFAULT.builder()
                        .setHeader("id", "firstName", "lastName", "email", "phone",
                                "position", "department", "salary", "status", "createdAt")
                        .build())) {
            for (Employee e : all) {
                printer.printRecord(
                        e.getId(), e.getFirstName(), e.getLastName(), e.getEmail(),
                        e.getPhone(), e.getPosition(), e.getDepartment(), e.getSalary(),
                        e.getStatus(), e.getCreatedAt() != null ? e.getCreatedAt().toString() : "");
            }
        } catch (IOException ex) {
            throw new RuntimeException("Erreur export CSV", ex);
        }
        return baos.toByteArray();
    }

    public byte[] exportJson() {
        List<EmployeeResponse> all = repository.findAll(Sort.by(Sort.Direction.ASC, "lastName"))
                .stream().map(mapper::toResponse).toList();
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
                    String email = safeGet(record, "email");
                    if (email != null && !email.isBlank() && repository.existsByEmail(email)) {
                        skipped++;
                        continue;
                    }
                    Employee emp = new Employee();
                    emp.setFirstName(record.get("firstName"));
                    emp.setLastName(record.get("lastName"));
                    emp.setEmail(email);
                    emp.setPhone(safeGet(record, "phone"));
                    emp.setPosition(record.get("position"));
                    emp.setDepartment(safeGet(record, "department"));
                    String salaryStr = safeGet(record, "salary");
                    if (salaryStr != null && !salaryStr.isBlank()) {
                        try { emp.setSalary(Double.parseDouble(salaryStr)); } catch (NumberFormatException ignored) {}
                    }
                    String statusStr = safeGet(record, "status");
                    emp.setStatus(statusStr != null && !statusStr.isBlank()
                            ? EmployeeStatus.valueOf(statusStr.toUpperCase()) : EmployeeStatus.ACTIVE);
                    repository.save(emp);
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
            List<EmployeeRequest> requests = objectMapper.readValue(file.getInputStream(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, EmployeeRequest.class));
            int idx = 1;
            for (EmployeeRequest req : requests) {
                try {
                    if (req.getEmail() != null && !req.getEmail().isBlank() && repository.existsByEmail(req.getEmail())) {
                        skipped++;
                    } else {
                        repository.save(mapper.toEntity(req));
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

    /** Lie (ou délie si userId == null) un compte utilisateur à l'employé, en vérifiant l'unicité. */
    private void applyUserLink(Employee emp, Long userId) {
        if (userId == null) {
            emp.setUser(null);
            return;
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Compte utilisateur introuvable"));
        repository.findByUser_Id(userId).ifPresent(other -> {
            if (emp.getId() == null || !other.getId().equals(emp.getId())) {
                throw new RuntimeException("Ce compte utilisateur est déjà lié à un autre employé");
            }
        });
        emp.setUser(user);
    }

    private Sort buildSort(String sortField, String sortOrder) {
        if (sortField == null || sortField.isBlank()) return Sort.unsorted();
        Sort.Direction direction = "desc".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(direction, sortField);
    }

    private static String safeGet(CSVRecord r, String col) {
        try { return r.get(col); } catch (IllegalArgumentException e) { return null; }
    }
}
