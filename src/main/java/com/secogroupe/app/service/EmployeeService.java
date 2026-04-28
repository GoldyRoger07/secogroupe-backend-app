package com.secogroupe.app.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.secogroupe.app.dto.EmployeeRequest;
import com.secogroupe.app.dto.EmployeeResponse;
import com.secogroupe.app.entity.Employee;
import com.secogroupe.app.mapper.EmployeeMapper;
import com.secogroupe.app.repository.EmployeeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository repository;
    private final EmployeeMapper mapper;

    public EmployeeResponse create(EmployeeRequest request) {
        Employee emp = mapper.toEntity(request);
        return mapper.toResponse(repository.save(emp));
    }

    public List<EmployeeResponse> getAll() {
        return repository.findAll()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    public EmployeeResponse getById(Long id) {
        Employee emp = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        return mapper.toResponse(emp);
    }

    public EmployeeResponse update(Long id, EmployeeRequest request) {
        Employee emp = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        mapper.update(emp, request);
        return mapper.toResponse(repository.save(emp));
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Employee not found");
        }
        repository.deleteById(id);
    }
}
