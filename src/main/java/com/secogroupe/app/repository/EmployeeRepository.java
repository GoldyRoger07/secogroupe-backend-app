package com.secogroupe.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.secogroupe.app.entity.Employee;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
}
