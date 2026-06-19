package com.secogroupe.app.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.secogroupe.app.dto.SessionResponse;
import com.secogroupe.app.service.SessionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    @PreAuthorize("hasAuthority('READ_SESSION')")
    @GetMapping
    public ResponseEntity<List<SessionResponse>> getAll() {
        return ResponseEntity.ok(sessionService.getAll());
    }

    @PreAuthorize("hasAuthority('DELETE_SESSION')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> revokeById(@PathVariable Long id) {
        sessionService.revokeById(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAuthority('DELETE_SESSION')")
    @DeleteMapping("/user/{userId}")
    public ResponseEntity<Void> revokeAllForUser(@PathVariable Long userId) {
        sessionService.revokeAllForUser(userId);
        return ResponseEntity.noContent().build();
    }
}
