package com.secogroupe.app.mapper;

import org.springframework.stereotype.Component;

import com.secogroupe.app.dto.EmployeeRequest;
import com.secogroupe.app.dto.EmployeeResponse;
import com.secogroupe.app.entity.Employee;

@Component
public class EmployeeMapper {

    public Employee toEntity(EmployeeRequest request) {
        Employee emp = new Employee();
        applyFields(emp, request);
        return emp;
    }

    /** Réponse complète — utilisée pour create/update (l'appelant a déjà les droits élevés). */
    public EmployeeResponse toResponse(Employee emp) {
        return toResponse(emp, true, true);
    }

    /**
     * Réponse filtrée selon les permissions du demandeur.
     *
     * @param canViewContact   READ_EMPLOYEE_CONTACT  → email, téléphone
     * @param canViewSensitive READ_EMPLOYEE_SENSITIVE → salaire, NIF, données personnelles, bancaires
     */
    public EmployeeResponse toResponse(Employee emp, boolean canViewContact, boolean canViewSensitive) {
        EmployeeResponse res = new EmployeeResponse();

        // ── Champs toujours publics ──────────────────────────
        res.setId(emp.getId());
        res.setFirstName(emp.getFirstName());
        res.setLastName(emp.getLastName());
        res.setDepartment(emp.getDepartment());
        res.setPosition(emp.getPosition());
        res.setPhotoUrl(emp.getPhotoUrl());
        res.setStatus(emp.getStatus());
        res.setCreatedAt(emp.getCreatedAt() != null ? emp.getCreatedAt().toString() : null);

        // ── Coordonnées (READ_EMPLOYEE_CONTACT) ──────────────
        if (canViewContact) {
            res.setEmail(emp.getEmail());
            res.setPhone(emp.getPhone());
        }

        // ── Données sensibles (READ_EMPLOYEE_SENSITIVE) ───────
        if (canViewSensitive) {
            res.setIdEmployee(emp.getIdEmployee());
            res.setSalary(emp.getSalary());
            res.setSexe(emp.getSexe());
            res.setNif(emp.getNif());
            res.setAdresse(emp.getAdresse());
            res.setEtatCivil(emp.getEtatCivil());
            res.setNombreEnfant(emp.getNombreEnfant());
            res.setDateEmbauche(emp.getDateEmbauche());
            res.setDateNaissance(emp.getDateNaissance());
            res.setBanque(emp.getBanque());
            res.setNumCompteBancaire(emp.getNumCompteBancaire());
            if (emp.getUser() != null) {
                res.setUserId(emp.getUser().getId());
                res.setUserUsername(emp.getUser().getUsername());
            }
        }

        return res;
    }

    public void update(Employee emp, EmployeeRequest request) {
        applyFields(emp, request);
    }

    private void applyFields(Employee emp, EmployeeRequest request) {
        emp.setFirstName(request.getFirstName());
        emp.setLastName(request.getLastName());
        emp.setEmail(request.getEmail());
        emp.setPhone(request.getPhone());
        emp.setPosition(request.getPosition());
        emp.setDepartment(request.getDepartment());
        emp.setSalary(request.getSalary());
        emp.setStatus(request.getStatus());
        emp.setSexe(request.getSexe());
        emp.setNif(request.getNif());
        emp.setAdresse(request.getAdresse());
        emp.setEtatCivil(request.getEtatCivil());
        emp.setNombreEnfant(request.getNombreEnfant() != null ? request.getNombreEnfant() : 0);
        emp.setDateEmbauche(request.getDateEmbauche());
        emp.setDateNaissance(request.getDateNaissance());
        emp.setBanque(request.getBanque());
        emp.setNumCompteBancaire(request.getNumCompteBancaire());
    }
}
