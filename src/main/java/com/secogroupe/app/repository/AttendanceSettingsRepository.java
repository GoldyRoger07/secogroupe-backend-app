package com.secogroupe.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.secogroupe.app.entity.AttendanceSettings;

public interface AttendanceSettingsRepository extends JpaRepository<AttendanceSettings, Long> {}
