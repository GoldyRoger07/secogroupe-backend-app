package com.secogroupe.app.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.secogroupe.app.dto.PageResponse;
import com.secogroupe.app.dto.PermissionRequest;
import com.secogroupe.app.dto.PermissionResponse;
import com.secogroupe.app.entity.Permission;
import com.secogroupe.app.mapper.PermissionMapper;
import com.secogroupe.app.repository.PermissionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;

    public PageResponse<PermissionResponse> getAll(int page, int size, String sortField, String sortOrder, String filter) {
        Sort sort = (sortField != null && !sortField.isBlank())
                ? Sort.by("desc".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC, sortField)
                : Sort.unsorted();
        Pageable pageable = PageRequest.of(page, size, sort);
        String f = (filter != null && !filter.isBlank()) ? filter : "";

        Page<Permission> resultPage = f.isEmpty()
                ? permissionRepository.findAll(pageable)
                : permissionRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrModuleContainingIgnoreCase(f, f, f, pageable);

        return new PageResponse<>(
                resultPage.getContent().stream().map(permissionMapper::toResponse).toList(),
                resultPage.getTotalElements(),
                resultPage.getTotalPages(),
                page,
                size);
    }

    public List<PermissionResponse> listAll() {
        return permissionRepository.findAll().stream().map(permissionMapper::toResponse).toList();
    }

    @Transactional
    public PermissionResponse create(PermissionRequest request) {
        if (permissionRepository.findByName(request.getName()).isPresent()) {
            throw new RuntimeException("Une permission avec ce nom existe déjà");
        }
        Permission permission = new Permission(request.getName());
        permission.setDescription(request.getDescription());
        permission.setModule(request.getModule());
        permission.setAction(request.getAction());
        return permissionMapper.toResponse(permissionRepository.save(permission));
    }

    @Transactional
    public PermissionResponse update(Long id, PermissionRequest request) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Permission introuvable"));

        permissionRepository.findByName(request.getName()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new RuntimeException("Une permission avec ce nom existe déjà");
            }
        });

        permission.setName(request.getName());
        permission.setDescription(request.getDescription());
        permission.setModule(request.getModule());
        permission.setAction(request.getAction());
        return permissionMapper.toResponse(permissionRepository.save(permission));
    }

    @Transactional
    public void delete(Long id) {
        if (!permissionRepository.existsById(id)) {
            throw new RuntimeException("Permission introuvable");
        }
        permissionRepository.deleteById(id);
    }
}
