package com.secogroupe.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.secogroupe.app.entity.EtatCivil;

public interface EtatCivilRepository extends JpaRepository<EtatCivil, Long> {
    boolean existsByNameIgnoreCase(String name);
    List<EtatCivil> findAllByOrderByNameAsc();
}
