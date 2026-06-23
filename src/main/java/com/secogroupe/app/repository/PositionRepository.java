package com.secogroupe.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.secogroupe.app.entity.Position;

public interface PositionRepository extends JpaRepository<Position, Long> {
    boolean existsByNameIgnoreCase(String name);
    List<Position> findAllByOrderByNameAsc();
}
