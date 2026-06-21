package com.secogroupe.app.service;

import java.time.Instant;
import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.secogroupe.app.dto.AttendanceCodeResponse;
import com.secogroupe.app.dto.AttendanceResponse;
import com.secogroupe.app.dto.PageResponse;
import com.secogroupe.app.dto.ScanResponse;
import com.secogroupe.app.entity.Attendance;
import com.secogroupe.app.entity.AttendanceStatus;
import com.secogroupe.app.entity.Employee;
import com.secogroupe.app.exception.ResourceNotFoundException;
import com.secogroupe.app.repository.AttendanceRepository;
import com.secogroupe.app.repository.EmployeeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;
    private final AttendanceCodeService codeService;

    // ──────────────── Code rotatif (admin) ────────────────

    public AttendanceCodeResponse currentCode() {
        return new AttendanceCodeResponse(
                codeService.currentCode(),
                AttendanceCodeService.PERIOD_SECONDS,
                codeService.secondsRemaining());
    }

    // ──────────────── Scan (employé) ────────────────

    @Transactional
    public ScanResponse scan(String code, String username) {
        if (!codeService.isValid(code)) {
            throw new RuntimeException("Code invalide ou expiré. Veuillez rescanner le QR à jour.");
        }

        Employee employee = employeeRepository.findByUser_Username(username)
                .orElseThrow(() -> new RuntimeException(
                        "Aucun employé n'est associé à votre compte. Contactez un administrateur."));

        String name = employee.getFirstName() + " " + employee.getLastName();
        LocalDate today = LocalDate.now();
        Instant now = Instant.now();

        Attendance attendance = attendanceRepository.findByEmployeeAndWorkDate(employee, today).orElse(null);

        if (attendance == null) {
            attendance = new Attendance();
            attendance.setEmployee(employee);
            attendance.setWorkDate(today);
            attendance.setCheckInAt(now);
            attendance.setStatus(AttendanceStatus.CHECKED_IN);
            attendanceRepository.save(attendance);
            return new ScanResponse("CHECKED_IN", "Arrivée enregistrée ✅", name, today, now);
        }

        if (attendance.getCheckOutAt() == null) {
            attendance.setCheckOutAt(now);
            attendance.setStatus(AttendanceStatus.CHECKED_OUT);
            attendanceRepository.save(attendance);
            return new ScanResponse("CHECKED_OUT", "Départ enregistré 👋", name, today, now);
        }

        return new ScanResponse("ALREADY_COMPLETE",
                "Votre présence est déjà complète pour aujourd'hui.", name, today, attendance.getCheckOutAt());
    }

    // ──────────────── Consultation (admin) ────────────────

    public PageResponse<AttendanceResponse> getAll(int page, int size, String sortField, String sortOrder, String filter) {
        Pageable pageable = buildPageable(page, size, sortField, sortOrder);
        String f = (filter != null && !filter.isBlank()) ? filter : "";

        Page<Attendance> result = f.isEmpty()
                ? attendanceRepository.findAll(pageable)
                : attendanceRepository.search(f, pageable);

        return toPageResponse(result, page, size);
    }

    // ──────────────── Mon historique (employé) ────────────────

    public PageResponse<AttendanceResponse> getMine(String username, int page, int size, String sortField, String sortOrder) {
        Employee employee = employeeRepository.findByUser_Username(username)
                .orElseThrow(() -> new ResourceNotFoundException("Aucun employé associé à ce compte"));
        Pageable pageable = buildPageable(page, size, sortField, sortOrder);
        Page<Attendance> result = attendanceRepository.findByEmployee(employee, pageable);
        return toPageResponse(result, page, size);
    }

    @Transactional
    public void delete(Long id) {
        if (!attendanceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Présence introuvable");
        }
        attendanceRepository.deleteById(id);
    }

    // ──────────────── helpers ────────────────

    private Pageable buildPageable(int page, int size, String sortField, String sortOrder) {
        String field = (sortField != null && !sortField.isBlank()) ? sortField : "workDate";
        // Le tri sur workDate puis checkInAt donne un ordre stable.
        Sort sort = Sort.by("desc".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC, field);
        return PageRequest.of(page, size, sort);
    }

    private PageResponse<AttendanceResponse> toPageResponse(Page<Attendance> result, int page, int size) {
        return new PageResponse<>(
                result.getContent().stream().map(this::toResponse).toList(),
                result.getTotalElements(),
                result.getTotalPages(),
                page,
                size);
    }

    private AttendanceResponse toResponse(Attendance a) {
        Employee e = a.getEmployee();
        AttendanceResponse r = new AttendanceResponse();
        r.setId(a.getId());
        r.setEmployeeId(e.getId());
        r.setEmployeeName(e.getFirstName() + " " + e.getLastName());
        r.setPosition(e.getPosition());
        r.setDepartment(e.getDepartment());
        r.setWorkDate(a.getWorkDate());
        r.setCheckInAt(a.getCheckInAt());
        r.setCheckOutAt(a.getCheckOutAt());
        r.setStatus(a.getStatus());
        return r;
    }
}
