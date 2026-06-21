package com.secogroupe.app.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.secogroupe.app.entity.Employee;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Page<Employee> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrPositionContainingIgnoreCaseOrDepartmentContainingIgnoreCase(
            String firstName, String lastName, String email, String position, String department, Pageable pageable);

    boolean existsByEmail(String email);

    Optional<Employee> findByUser_Username(String username);

    Optional<Employee> findByUser_Id(Long userId);
}
