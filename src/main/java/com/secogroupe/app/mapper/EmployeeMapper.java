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
        emp.setPosition(request.getPosition());
        emp.setSalary(request.getSalary());
        return emp;
    }

    public EmployeeResponse toResponse(Employee emp) {
        EmployeeResponse res = new EmployeeResponse();
        res.setId(emp.getId());
        res.setFirstName(emp.getFirstName());
        res.setLastName(emp.getLastName());
        res.setPosition(emp.getPosition());
        res.setSalary(emp.getSalary());
        return res;
    }

    public void update(Employee emp, EmployeeRequest request) {
        emp.setFirstName(request.getFirstName());
        emp.setLastName(request.getLastName());
        emp.setPosition(request.getPosition());
        emp.setSalary(request.getSalary());
    }
}
