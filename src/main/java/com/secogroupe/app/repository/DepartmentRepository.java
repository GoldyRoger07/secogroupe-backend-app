package com.secogroupe.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.secogroupe.app.entity.Department;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    boolean existsByNameIgnoreCase(String name);
    List<Department> findAllByOrderByNameAsc();
}
