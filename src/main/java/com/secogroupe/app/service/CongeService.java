package com.secogroupe.app.service;

import java.time.Instant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.secogroupe.app.dto.CongeRequest;
import com.secogroupe.app.dto.CongeResponse;
import com.secogroupe.app.dto.PageResponse;
import com.secogroupe.app.entity.Conge;
import com.secogroupe.app.entity.Employee;
import com.secogroupe.app.mapper.CongeMapper;
import com.secogroupe.app.model.CongeStatus;
import com.secogroupe.app.repository.CongeRepository;
import com.secogroupe.app.repository.EmployeeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CongeService {

    private final CongeRepository congeRepository;
    private final EmployeeRepository employeeRepository;
    private final CongeMapper mapper;

    public PageResponse<CongeResponse> getAll(int page, int size, String sortField, String sortOrder, String filter) {
        Sort sort = buildSort(sortField, sortOrder);
        Pageable pageable = PageRequest.of(page, size, sort);
        String f = (filter != null && !filter.isBlank()) ? filter : "";

        Page<Conge> resultPage = f.isEmpty()
                ? congeRepository.findAll(pageable)
                : congeRepository.search(f, pageable);

        return new PageResponse<>(
                resultPage.getContent().stream().map(mapper::toResponse).toList(),
                resultPage.getTotalElements(),
                resultPage.getTotalPages(),
                page,
                size);
    }

    public CongeResponse getById(Long id) {
        return mapper.toResponse(findOrThrow(id));
    }

    public CongeResponse create(CongeRequest request) {
        Employee employee = findEmployee(request.getEmployeeId());
        Employee approvedBy = request.getApprovedById() != null ? findEmployee(request.getApprovedById()) : null;
        return mapper.toResponse(congeRepository.save(mapper.toEntity(request, employee, approvedBy)));
    }

    public CongeResponse update(Long id, CongeRequest request) {
        Conge conge = findOrThrow(id);
        Employee employee = findEmployee(request.getEmployeeId());
        Employee approvedBy = request.getApprovedById() != null ? findEmployee(request.getApprovedById()) : null;
        mapper.update(conge, request, employee, approvedBy);
        return mapper.toResponse(congeRepository.save(conge));
    }

    public CongeResponse updateStatus(Long id, CongeStatus status, String managerComment) {
        Conge conge = findOrThrow(id);
        conge.setStatus(status);
        if (managerComment != null) conge.setManagerComment(managerComment);
        conge.setDecisionDate(Instant.now());
        conge.setUpdatedAt(Instant.now());
        return mapper.toResponse(congeRepository.save(conge));
    }

    public void delete(Long id) {
        congeRepository.delete(findOrThrow(id));
    }

    private Conge findOrThrow(Long id) {
        return congeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Congé introuvable"));
    }

    private Employee findEmployee(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employé introuvable"));
    }

    private Sort buildSort(String sortField, String sortOrder) {
        if (sortField == null || sortField.isBlank()) return Sort.by(Sort.Direction.DESC, "createdAt");
        return Sort.by("desc".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC, sortField);
    }
}
