package com.secogroupe.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.secogroupe.app.entity.TypeConge;

public interface TypeCongeRepository extends JpaRepository<TypeConge, Long> {
    boolean existsByNameIgnoreCase(String name);
    List<TypeConge> findAllByOrderByNameAsc();
}
