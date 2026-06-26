package com.secogroupe.app.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.secogroupe.app.dto.ApplicationRequestDto;
import com.secogroupe.app.dto.ApplicationResponse;
import com.secogroupe.app.dto.ApplicationUpdateRequest;
import com.secogroupe.app.dto.PageResponse;
import com.secogroupe.app.dto.SseEvent;
import com.secogroupe.app.entity.JobApplication;
import com.secogroupe.app.exception.ResourceNotFoundException;
import com.secogroupe.app.repository.JobApplicationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final JobApplicationRepository jobApplicationRepository;
    private final EmailService emailService;
    private final ObjectMapper objectMapper;
    private final SseEmitterService sseEmitterService;

    // ──────────────── Public submission ────────────────

    @Transactional
    public void submit(ApplicationRequestDto dto) {
        JobApplication entity = new JobApplication();
        entity.setFirstName(dto.getFirstName());
        entity.setLastName(dto.getLastName());
        entity.setEmail(dto.getEmail());
        entity.setPhone(dto.getPhone());
        entity.setPosition(dto.getPosition());
        JobApplication saved = jobApplicationRepository.save(entity);

        sseEmitterService.broadcast(new SseEvent(
                "NEW_APPLICATION",
                "Nouvelle candidature",
                dto.getFirstName() + " " + dto.getLastName() + " — " + dto.getPosition(),
                saved.getId()
        ));

        try {
            emailService.sendApplicationNotification(dto);
        } catch (Exception e) {
            log.error("Échec notification interne pour candidature de {}: {}", dto.getEmail(), e.getMessage());
        }

        try {
            emailService.sendApplicationConfirmation(dto);
        } catch (Exception e) {
            log.error("Échec accusé de réception pour {}: {}", dto.getEmail(), e.getMessage());
        }
    }

    // ──────────────── Management (admin) ────────────────

    public PageResponse<ApplicationResponse> getAll(int page, int size, String sortField, String sortOrder, String filter) {
        String field = (sortField != null && !sortField.isBlank()) ? sortField : "submittedAt";
        Sort sort = Sort.by("desc".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC, field);
        Pageable pageable = PageRequest.of(page, size, sort);
        String f = (filter != null && !filter.isBlank()) ? filter : "";

        Page<JobApplication> result = f.isEmpty()
                ? jobApplicationRepository.findAll(pageable)
                : jobApplicationRepository.search(f, pageable);

        return new PageResponse<>(
                result.getContent().stream().map(this::toResponse).toList(),
                result.getTotalElements(),
                result.getTotalPages(),
                page,
                size);
    }

    public ApplicationResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional
    public ApplicationResponse update(Long id, ApplicationUpdateRequest request) {
        JobApplication application = findOrThrow(id);
        application.setStatus(request.getStatus());
        application.setAdminNotes(request.getAdminNotes());
        return toResponse(jobApplicationRepository.save(application));
    }

    @Transactional
    public void delete(Long id) {
        if (!jobApplicationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Candidature introuvable");
        }
        jobApplicationRepository.deleteById(id);
    }

    // ──────────────── Export ────────────────

    public byte[] exportCsv() {
        List<JobApplication> all = jobApplicationRepository.findAll(Sort.by(Sort.Direction.DESC, "submittedAt"));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (CSVPrinter printer = new CSVPrinter(
                new OutputStreamWriter(baos, StandardCharsets.UTF_8),
                CSVFormat.DEFAULT.builder()
                        .setHeader("id", "firstName", "lastName", "email", "phone",
                                "position", "status", "adminNotes", "submittedAt")
                        .build())) {
            for (JobApplication a : all) {
                printer.printRecord(
                        a.getId(), a.getFirstName(), a.getLastName(), a.getEmail(),
                        a.getPhone(), a.getPosition(), a.getStatus(),
                        a.getAdminNotes(), a.getSubmittedAt() != null ? a.getSubmittedAt().toString() : "");
            }
        } catch (IOException ex) {
            throw new RuntimeException("Erreur export CSV", ex);
        }
        return baos.toByteArray();
    }

    public byte[] exportJson() {
        List<ApplicationResponse> all = jobApplicationRepository.findAll(Sort.by(Sort.Direction.DESC, "submittedAt"))
                .stream().map(this::toResponse).toList();
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(all);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException("Erreur export JSON", ex);
        }
    }

    // ──────────────── helpers ────────────────

    private JobApplication findOrThrow(Long id) {
        return jobApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Candidature introuvable"));
    }

    private ApplicationResponse toResponse(JobApplication a) {
        ApplicationResponse r = new ApplicationResponse();
        r.setId(a.getId());
        r.setFirstName(a.getFirstName());
        r.setLastName(a.getLastName());
        r.setEmail(a.getEmail());
        r.setPhone(a.getPhone());
        r.setPosition(a.getPosition());
        r.setStatus(a.getStatus());
        r.setAdminNotes(a.getAdminNotes());
        r.setSubmittedAt(a.getSubmittedAt());
        return r;
    }
}
