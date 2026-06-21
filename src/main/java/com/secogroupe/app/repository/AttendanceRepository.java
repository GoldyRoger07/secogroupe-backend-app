package com.secogroupe.app.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.secogroupe.app.entity.Attendance;
import com.secogroupe.app.entity.Employee;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    Optional<Attendance> findByEmployeeAndWorkDate(Employee employee, LocalDate workDate);

    Page<Attendance> findByEmployee(Employee employee, Pageable pageable);

    @Query("""
            SELECT a FROM Attendance a WHERE
            LOWER(a.employee.firstName)  LIKE LOWER(CONCAT('%', :f, '%')) OR
            LOWER(a.employee.lastName)   LIKE LOWER(CONCAT('%', :f, '%')) OR
            LOWER(a.employee.email)      LIKE LOWER(CONCAT('%', :f, '%')) OR
            LOWER(a.employee.position)   LIKE LOWER(CONCAT('%', :f, '%')) OR
            LOWER(a.employee.department) LIKE LOWER(CONCAT('%', :f, '%'))
            """)
    Page<Attendance> search(@Param("f") String filter, Pageable pageable);
}
