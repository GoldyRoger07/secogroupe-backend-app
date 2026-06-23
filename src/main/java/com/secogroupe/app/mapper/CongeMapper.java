package com.secogroupe.app.mapper;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Component;

import com.secogroupe.app.dto.CongeRequest;
import com.secogroupe.app.dto.CongeResponse;
import com.secogroupe.app.entity.Conge;
import com.secogroupe.app.entity.Employee;
import com.secogroupe.app.model.CongeStatus;

@Component
public class CongeMapper {

    public Conge toEntity(CongeRequest request, Employee employee, Employee approvedBy) {
        Conge conge = new Conge();
        applyFields(conge, request, employee, approvedBy);
        conge.setCreatedAt(Instant.now());
        return conge;
    }

    public CongeResponse toResponse(Conge conge) {
        CongeResponse res = new CongeResponse();
        res.setId(conge.getId());
        if (conge.getEmployee() != null) {
            res.setEmployeeId(conge.getEmployee().getId());
            res.setEmployeeFullName(conge.getEmployee().getFirstName() + " " + conge.getEmployee().getLastName());
        }
        res.setTypeConge(conge.getTypeConge());
        res.setStartDate(conge.getStartDate());
        res.setEndDate(conge.getEndDate());
        res.setNumberOfDays(conge.getNumberOfDays());
        res.setReason(conge.getReason());
        res.setStatus(conge.getStatus());
        if (conge.getApprovedBy() != null) {
            res.setApprovedById(conge.getApprovedBy().getId());
            res.setApprovedByFullName(conge.getApprovedBy().getFirstName() + " " + conge.getApprovedBy().getLastName());
        }
        res.setManagerComment(conge.getManagerComment());
        res.setDecisionDate(conge.getDecisionDate() != null ? conge.getDecisionDate().toString() : null);
        res.setCreatedAt(conge.getCreatedAt() != null ? conge.getCreatedAt().toString() : null);
        res.setUpdatedAt(conge.getUpdatedAt() != null ? conge.getUpdatedAt().toString() : null);
        return res;
    }

    public void update(Conge conge, CongeRequest request, Employee employee, Employee approvedBy) {
        applyFields(conge, request, employee, approvedBy);
        conge.setUpdatedAt(Instant.now());
    }

    private void applyFields(Conge conge, CongeRequest request, Employee employee, Employee approvedBy) {
        conge.setEmployee(employee);
        conge.setTypeConge(request.getTypeConge());
        conge.setStartDate(request.getStartDate());
        conge.setEndDate(request.getEndDate());
        if (request.getNumberOfDays() != null) {
            conge.setNumberOfDays(request.getNumberOfDays());
        } else if (request.getStartDate() != null && request.getEndDate() != null) {
            long days = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;
            conge.setNumberOfDays((int) Math.max(days, 1));
        }
        conge.setReason(request.getReason());
        conge.setStatus(request.getStatus() != null ? request.getStatus() : CongeStatus.PENDING);
        conge.setApprovedBy(approvedBy);
        conge.setManagerComment(request.getManagerComment());
    }
}
