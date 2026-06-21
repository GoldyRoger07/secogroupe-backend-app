package com.secogroupe.app.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.secogroupe.app.dto.AttendanceCodeResponse;
import com.secogroupe.app.dto.AttendanceResponse;
import com.secogroupe.app.dto.AttendanceSettingsRequest;
import com.secogroupe.app.dto.AttendanceSettingsResponse;
import com.secogroupe.app.dto.PageResponse;
import com.secogroupe.app.dto.ScanResponse;
import com.secogroupe.app.entity.ArrivalStatus;
import com.secogroupe.app.entity.Attendance;
import com.secogroupe.app.entity.AttendanceSettings;
import com.secogroupe.app.entity.AttendanceStatus;
import com.secogroupe.app.entity.DepartureStatus;
import com.secogroupe.app.entity.Employee;
import com.secogroupe.app.exception.ResourceNotFoundException;
import com.secogroupe.app.repository.AttendanceRepository;
import com.secogroupe.app.repository.AttendanceSettingsRepository;
import com.secogroupe.app.repository.EmployeeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;
    private final AttendanceCodeService codeService;
    private final AttendanceSettingsRepository settingsRepository;

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
        AttendanceSettings settings = getOrCreateSettings();

        Attendance attendance = attendanceRepository.findByEmployeeAndWorkDate(employee, today).orElse(null);

        if (attendance == null) {
            ArrivalStatus arrival = evaluateArrival(now, settings);
            attendance = new Attendance();
            attendance.setEmployee(employee);
            attendance.setWorkDate(today);
            attendance.setCheckInAt(now);
            attendance.setStatus(AttendanceStatus.CHECKED_IN);
            attendance.setArrivalStatus(arrival);
            attendanceRepository.save(attendance);
            return new ScanResponse("CHECKED_IN", arrivalMessage(arrival), name, today, now,
                    arrival.name(), null);
        }

        if (attendance.getCheckOutAt() == null) {
            DepartureStatus departure = evaluateDeparture(now, settings);
            attendance.setCheckOutAt(now);
            attendance.setStatus(AttendanceStatus.CHECKED_OUT);
            attendance.setDepartureStatus(departure);
            attendanceRepository.save(attendance);
            return new ScanResponse("CHECKED_OUT", departureMessage(departure), name, today, now,
                    attendance.getArrivalStatus() != null ? attendance.getArrivalStatus().name() : null,
                    departure.name());
        }

        return new ScanResponse("ALREADY_COMPLETE",
                "Votre présence est déjà complète pour aujourd'hui.", name, today, attendance.getCheckOutAt(),
                attendance.getArrivalStatus() != null ? attendance.getArrivalStatus().name() : null,
                attendance.getDepartureStatus() != null ? attendance.getDepartureStatus().name() : null);
    }

    // ──────────────── Ponctualité ────────────────

    private ArrivalStatus evaluateArrival(Instant moment, AttendanceSettings settings) {
        LocalTime actual = LocalTime.ofInstant(moment, ZoneId.systemDefault());
        LocalTime expected = settings.getExpectedCheckIn();
        long grace = settings.getGraceMinutes();
        if (actual.isAfter(expected.plusMinutes(grace))) return ArrivalStatus.LATE;
        if (actual.isBefore(expected.minusMinutes(grace))) return ArrivalStatus.EARLY;
        return ArrivalStatus.ON_TIME;
    }

    private DepartureStatus evaluateDeparture(Instant moment, AttendanceSettings settings) {
        LocalTime actual = LocalTime.ofInstant(moment, ZoneId.systemDefault());
        LocalTime expected = settings.getExpectedCheckOut();
        long grace = settings.getGraceMinutes();
        if (actual.isAfter(expected.plusMinutes(grace))) return DepartureStatus.OVERTIME;
        if (actual.isBefore(expected.minusMinutes(grace))) return DepartureStatus.EARLY;
        return DepartureStatus.ON_TIME;
    }

    private String arrivalMessage(ArrivalStatus status) {
        return switch (status) {
            case EARLY -> "Arrivée enregistrée — en avance ✅";
            case ON_TIME -> "Arrivée enregistrée — à l'heure ✅";
            case LATE -> "Arrivée enregistrée — en retard ⏰";
        };
    }

    private String departureMessage(DepartureStatus status) {
        return switch (status) {
            case EARLY -> "Départ enregistré — anticipé 👋";
            case ON_TIME -> "Départ enregistré — à l'heure 👋";
            case OVERTIME -> "Départ enregistré — heures supplémentaires 👏";
        };
    }

    // ──────────────── Paramètres horaires ────────────────

    public AttendanceSettingsResponse getSettings() {
        AttendanceSettings s = getOrCreateSettings();
        return new AttendanceSettingsResponse(s.getExpectedCheckIn(), s.getExpectedCheckOut(), s.getGraceMinutes());
    }

    @Transactional
    public AttendanceSettingsResponse updateSettings(AttendanceSettingsRequest request) {
        AttendanceSettings s = getOrCreateSettings();
        s.setExpectedCheckIn(request.getExpectedCheckIn());
        s.setExpectedCheckOut(request.getExpectedCheckOut());
        s.setGraceMinutes(request.getGraceMinutes());
        settingsRepository.save(s);
        return new AttendanceSettingsResponse(s.getExpectedCheckIn(), s.getExpectedCheckOut(), s.getGraceMinutes());
    }

    private AttendanceSettings getOrCreateSettings() {
        return settingsRepository.findAll().stream().findFirst()
                .orElseGet(() -> settingsRepository.save(new AttendanceSettings()));
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
        r.setArrivalStatus(a.getArrivalStatus());
        r.setDepartureStatus(a.getDepartureStatus());
        return r;
    }
}
