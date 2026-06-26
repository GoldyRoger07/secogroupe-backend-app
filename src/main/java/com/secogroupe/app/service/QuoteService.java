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
import com.secogroupe.app.dto.PageResponse;
import com.secogroupe.app.dto.QuoteRequestDto;
import com.secogroupe.app.dto.QuoteResponse;
import com.secogroupe.app.dto.QuoteUpdateRequest;
import com.secogroupe.app.dto.SseEvent;
import com.secogroupe.app.entity.QuoteRequest;
import com.secogroupe.app.exception.ResourceNotFoundException;
import com.secogroupe.app.repository.QuoteRequestRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuoteService {

    private final QuoteRequestRepository quoteRequestRepository;
    private final EmailService emailService;
    private final ObjectMapper objectMapper;
    private final SseEmitterService sseEmitterService;

    // ──────────────── Public submission ────────────────

    @Transactional
    public void submit(QuoteRequestDto dto) {
        QuoteRequest entity = new QuoteRequest();
        entity.setServiceCategory(dto.getServiceCategory());
        entity.setBusinessName(dto.getBusinessName());
        entity.setDirectDialNumber(dto.getDirectDialNumber());
        entity.setFirstName(dto.getFirstName());
        entity.setLastName(dto.getLastName());
        entity.setCity(dto.getCity());
        entity.setState(dto.getState());
        entity.setEmail(dto.getEmail());
        entity.setNewsletterOptIn(dto.isNewsletterOptIn());
        QuoteRequest saved = quoteRequestRepository.save(entity);

        sseEmitterService.broadcast(new SseEvent(
                "NEW_QUOTE",
                "Nouvelle demande de devis",
                dto.getFirstName() + " " + dto.getLastName() + " — " + dto.getServiceCategory(),
                saved.getId()
        ));

        try {
            emailService.sendQuoteNotification(dto);
        } catch (Exception e) {
            log.error("Échec notification interne pour devis de {}: {}", dto.getEmail(), e.getMessage());
        }

        try {
            emailService.sendQuoteConfirmation(dto);
        } catch (Exception e) {
            log.error("Échec accusé de réception pour {}: {}", dto.getEmail(), e.getMessage());
        }
    }

    // ──────────────── Management (admin) ────────────────

    public PageResponse<QuoteResponse> getAll(int page, int size, String sortField, String sortOrder, String filter) {
        String field = (sortField != null && !sortField.isBlank()) ? sortField : "submittedAt";
        Sort sort = Sort.by("desc".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC, field);
        Pageable pageable = PageRequest.of(page, size, sort);
        String f = (filter != null && !filter.isBlank()) ? filter : "";

        Page<QuoteRequest> result = f.isEmpty()
                ? quoteRequestRepository.findAll(pageable)
                : quoteRequestRepository.search(f, pageable);

        return new PageResponse<>(
                result.getContent().stream().map(this::toResponse).toList(),
                result.getTotalElements(),
                result.getTotalPages(),
                page,
                size);
    }

    public QuoteResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional
    public QuoteResponse update(Long id, QuoteUpdateRequest request) {
        QuoteRequest quote = findOrThrow(id);
        quote.setStatus(request.getStatus());
        quote.setAdminNotes(request.getAdminNotes());
        return toResponse(quoteRequestRepository.save(quote));
    }

    @Transactional
    public void delete(Long id) {
        if (!quoteRequestRepository.existsById(id)) {
            throw new ResourceNotFoundException("Demande de devis introuvable");
        }
        quoteRequestRepository.deleteById(id);
    }

    // ──────────────── Export ────────────────

    public byte[] exportCsv() {
        List<QuoteRequest> all = quoteRequestRepository.findAll(Sort.by(Sort.Direction.DESC, "submittedAt"));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (CSVPrinter printer = new CSVPrinter(
                new OutputStreamWriter(baos, StandardCharsets.UTF_8),
                CSVFormat.DEFAULT.builder()
                        .setHeader("id", "firstName", "lastName", "email", "directDialNumber",
                                "serviceCategory", "businessName", "city", "state",
                                "newsletterOptIn", "status", "adminNotes", "submittedAt")
                        .build())) {
            for (QuoteRequest q : all) {
                printer.printRecord(
                        q.getId(), q.getFirstName(), q.getLastName(), q.getEmail(),
                        q.getDirectDialNumber(), q.getServiceCategory(), q.getBusinessName(),
                        q.getCity(), q.getState(), q.isNewsletterOptIn(), q.getStatus(),
                        q.getAdminNotes(), q.getSubmittedAt() != null ? q.getSubmittedAt().toString() : "");
            }
        } catch (IOException ex) {
            throw new RuntimeException("Erreur export CSV", ex);
        }
        return baos.toByteArray();
    }

    public byte[] exportJson() {
        List<QuoteResponse> all = quoteRequestRepository.findAll(Sort.by(Sort.Direction.DESC, "submittedAt"))
                .stream().map(this::toResponse).toList();
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(all);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException("Erreur export JSON", ex);
        }
    }

    // ──────────────── helpers ────────────────

    private QuoteRequest findOrThrow(Long id) {
        return quoteRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Demande de devis introuvable"));
    }

    private QuoteResponse toResponse(QuoteRequest q) {
        QuoteResponse r = new QuoteResponse();
        r.setId(q.getId());
        r.setServiceCategory(q.getServiceCategory());
        r.setBusinessName(q.getBusinessName());
        r.setDirectDialNumber(q.getDirectDialNumber());
        r.setFirstName(q.getFirstName());
        r.setLastName(q.getLastName());
        r.setCity(q.getCity());
        r.setState(q.getState());
        r.setEmail(q.getEmail());
        r.setNewsletterOptIn(q.isNewsletterOptIn());
        r.setStatus(q.getStatus());
        r.setAdminNotes(q.getAdminNotes());
        r.setSubmittedAt(q.getSubmittedAt());
        return r;
    }
}
