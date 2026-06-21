package com.secogroupe.app.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.secogroupe.app.entity.ApplicationStatus;
import com.secogroupe.app.entity.JobApplication;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {

    @Query("""
            SELECT a FROM JobApplication a WHERE
            LOWER(a.firstName) LIKE LOWER(CONCAT('%', :f, '%')) OR
            LOWER(a.lastName)  LIKE LOWER(CONCAT('%', :f, '%')) OR
            LOWER(a.email)     LIKE LOWER(CONCAT('%', :f, '%')) OR
            LOWER(a.phone)     LIKE LOWER(CONCAT('%', :f, '%')) OR
            LOWER(a.position)  LIKE LOWER(CONCAT('%', :f, '%'))
            """)
    Page<JobApplication> search(@Param("f") String filter, Pageable pageable);

    long countByStatus(ApplicationStatus status);
}
