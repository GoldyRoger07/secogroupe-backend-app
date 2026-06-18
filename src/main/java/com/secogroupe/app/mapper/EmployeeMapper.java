package com.secogroupe.app.mapper;

import org.springframework.stereotype.Component;

import com.secogroupe.app.dto.EmployeeRequest;
import com.secogroupe.app.dto.EmployeeResponse;
import com.secogroupe.app.entity.Employee;

@Component
public class EmployeeMapper {

    public Employee toEntity(EmployeeRequest request) {
        Employee emp = new Employee();
        emp.setFirstName(request.getFirstName());
        emp.setLastName(request.getLastName());
        emp.setEmail(request.getEmail());
        emp.setPhone(request.getPhone());
        emp.setPosition(request.getPosition());
        emp.setDepartment(request.getDepartment());
        emp.setSalary(request.getSalary());
        emp.setStatus(request.getStatus());
        return emp;
    }

    public EmployeeResponse toResponse(Employee emp) {
        EmployeeResponse res = new EmployeeResponse();
        res.setId(emp.getId());
        res.setFirstName(emp.getFirstName());
        res.setLastName(emp.getLastName());
        res.setEmail(emp.getEmail());
        res.setPhone(emp.getPhone());
        res.setPosition(emp.getPosition());
        res.setDepartment(emp.getDepartment());
        res.setSalary(emp.getSalary());
        res.setPhotoUrl(emp.getPhotoUrl());
        res.setStatus(emp.getStatus());
        res.setCreatedAt(emp.getCreatedAt() != null ? emp.getCreatedAt().toString() : null);
        return res;
    }

    public void update(Employee emp, EmployeeRequest request) {
        emp.setFirstName(request.getFirstName());
        emp.setLastName(request.getLastName());
        emp.setEmail(request.getEmail());
        emp.setPhone(request.getPhone());
        emp.setPosition(request.getPosition());
        emp.setDepartment(request.getDepartment());
        emp.setSalary(request.getSalary());
        emp.setStatus(request.getStatus());
    }
}
