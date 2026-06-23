package com.secogroupe.app.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.secogroupe.app.entity.Conge;

public interface CongeRepository extends JpaRepository<Conge, Long> {

    @Query("SELECT c FROM Conge c WHERE " +
           "LOWER(c.employee.firstName) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(c.employee.lastName)  LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(c.typeConge)          LIKE LOWER(CONCAT('%', :q, '%'))")
    Page<Conge> search(@Param("q") String q, Pageable pageable);
}
