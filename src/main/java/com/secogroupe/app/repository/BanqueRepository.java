package com.secogroupe.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.secogroupe.app.entity.Banque;

public interface BanqueRepository extends JpaRepository<Banque, Long> {
    boolean existsByNameIgnoreCase(String name);
    List<Banque> findAllByOrderByNameAsc();
}
