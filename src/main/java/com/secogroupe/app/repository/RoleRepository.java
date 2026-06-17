package com.secogroupe.app.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.secogroupe.app.entity.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
}

