package com.secogroupe.app.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.Data;

@Entity
@Data
public class Employee {
    @Id 
    @GeneratedValue
    private Long id;

    private String firstName;
    private String lastName;
    private String position;
    private Double salary;

    @OneToOne
    private User user;
}