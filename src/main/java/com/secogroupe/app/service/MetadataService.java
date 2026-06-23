package com.secogroupe.app.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.secogroupe.app.entity.Banque;
import com.secogroupe.app.entity.Department;
import com.secogroupe.app.entity.EtatCivil;
import com.secogroupe.app.entity.Position;
import com.secogroupe.app.entity.TypeConge;
import com.secogroupe.app.repository.BanqueRepository;
import com.secogroupe.app.repository.DepartmentRepository;
import com.secogroupe.app.repository.EtatCivilRepository;
import com.secogroupe.app.repository.PositionRepository;
import com.secogroupe.app.repository.TypeCongeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MetadataService {

    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;
    private final BanqueRepository banqueRepository;
    private final EtatCivilRepository etatCivilRepository;
    private final TypeCongeRepository typeCongeRepository;

    // ──────────────── Departments ────────────────

    public List<String> getDepartments() {
        return departmentRepository.findAllByOrderByNameAsc()
                .stream().map(Department::getName).toList();
    }

    public String createDepartment(String name) {
        if (departmentRepository.existsByNameIgnoreCase(name)) return name;
        Department dept = new Department();
        dept.setName(name.trim());
        return departmentRepository.save(dept).getName();
    }

    // ──────────────── Positions ────────────────

    public List<String> getPositions() {
        return positionRepository.findAllByOrderByNameAsc()
                .stream().map(Position::getName).toList();
    }

    public String createPosition(String name) {
        if (positionRepository.existsByNameIgnoreCase(name)) return name;
        Position pos = new Position();
        pos.setName(name.trim());
        return positionRepository.save(pos).getName();
    }

    // ──────────────── Banques ────────────────

    public List<String> getBanques() {
        return banqueRepository.findAllByOrderByNameAsc()
                .stream().map(Banque::getName).toList();
    }

    public String createBanque(String name) {
        if (banqueRepository.existsByNameIgnoreCase(name)) return name;
        Banque banque = new Banque();
        banque.setName(name.trim());
        return banqueRepository.save(banque).getName();
    }

    // ──────────────── États civils ────────────────

    public List<String> getEtatsCivils() {
        return etatCivilRepository.findAllByOrderByNameAsc()
                .stream().map(EtatCivil::getName).toList();
    }

    public String createEtatCivil(String name) {
        if (etatCivilRepository.existsByNameIgnoreCase(name)) return name;
        EtatCivil etatCivil = new EtatCivil();
        etatCivil.setName(name.trim());
        return etatCivilRepository.save(etatCivil).getName();
    }

    // ──────────────── Types de congé ────────────────

    public List<String> getTypesConges() {
        return typeCongeRepository.findAllByOrderByNameAsc()
                .stream().map(TypeConge::getName).toList();
    }

    public String createTypeConge(String name) {
        if (typeCongeRepository.existsByNameIgnoreCase(name)) return name;
        TypeConge tc = new TypeConge();
        tc.setName(name.trim());
        return typeCongeRepository.save(tc).getName();
    }
}
