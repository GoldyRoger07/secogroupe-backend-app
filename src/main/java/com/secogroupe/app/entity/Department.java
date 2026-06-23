package com.secogroupe.app.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "departments")
@Data
public class Department {

    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;
}
