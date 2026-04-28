package com.secogroupe.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.secogroupe.app.entity.Permission;

public interface PermissionRepository extends JpaRepository<Permission, Long> {}
