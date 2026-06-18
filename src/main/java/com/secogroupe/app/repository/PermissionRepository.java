package com.secogroupe.app.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.secogroupe.app.entity.Permission;

public interface PermissionRepository extends JpaRepository<Permission, Long> {

    Optional<Permission> findByName(String name);

    Page<Permission> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrModuleContainingIgnoreCase(
            String name, String description, String module, Pageable pageable);
}
