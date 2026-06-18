package com.secogroupe.app.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.secogroupe.app.entity.User;
import com.secogroupe.app.entity.UserPreferences;

public interface UserPreferencesRepository extends JpaRepository<UserPreferences, Long> {
    Optional<UserPreferences> findByUser(User user);
}
