package com.secogroupe.app.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.secogroupe.app.dto.EmployeeRequest;
import com.secogroupe.app.dto.EmployeeResponse;
import com.secogroupe.app.dto.PageResponse;
import com.secogroupe.app.entity.Employee;
import com.secogroupe.app.mapper.EmployeeMapper;
import com.secogroupe.app.repository.EmployeeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository repository;
    private final EmployeeMapper mapper;
    private final PhotoStorageService photoStorageService;

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
        if (photo != null && !photo.isEmpty()) {
            emp.setPhotoUrl(photoStorageService.store(photo));
        }
        return mapper.toResponse(repository.save(emp));
    }

    public EmployeeResponse update(Long id, EmployeeRequest request, MultipartFile photo) {
        Employee emp = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employé introuvable"));
        mapper.update(emp, request);
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

    private Sort buildSort(String sortField, String sortOrder) {
        if (sortField == null || sortField.isBlank()) return Sort.unsorted();
        Sort.Direction direction = "desc".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(direction, sortField);
    }
}
