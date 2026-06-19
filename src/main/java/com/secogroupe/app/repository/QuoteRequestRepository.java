package com.secogroupe.app.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.secogroupe.app.entity.QuoteRequest;
import com.secogroupe.app.entity.QuoteStatus;

public interface QuoteRequestRepository extends JpaRepository<QuoteRequest, Long> {

    @Query("""
            SELECT q FROM QuoteRequest q WHERE
            LOWER(q.firstName)        LIKE LOWER(CONCAT('%', :f, '%')) OR
            LOWER(q.lastName)         LIKE LOWER(CONCAT('%', :f, '%')) OR
            LOWER(q.email)            LIKE LOWER(CONCAT('%', :f, '%')) OR
            LOWER(q.businessName)     LIKE LOWER(CONCAT('%', :f, '%')) OR
            LOWER(q.serviceCategory)  LIKE LOWER(CONCAT('%', :f, '%')) OR
            LOWER(q.city)             LIKE LOWER(CONCAT('%', :f, '%')) OR
            LOWER(q.state)            LIKE LOWER(CONCAT('%', :f, '%'))
            """)
    Page<QuoteRequest> search(@Param("f") String filter, Pageable pageable);

    long countByStatus(QuoteStatus status);
}
