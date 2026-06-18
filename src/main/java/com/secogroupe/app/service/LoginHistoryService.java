package com.secogroupe.app.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.secogroupe.app.entity.LoginHistory;
import com.secogroupe.app.repository.LoginHistoryRepository;
import com.secogroupe.app.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoginHistoryService {

    private final LoginHistoryRepository loginHistoryRepository;
    private final UserRepository userRepository;

    @Transactional
    public void record(String username, String ip, String device, boolean success) {
        userRepository.findByUsername(username).ifPresent(user -> {
            LoginHistory history = new LoginHistory();
            history.setUser(user);
            history.setIpAddress(ip != null ? ip : "Inconnu");
            history.setDevice(device != null ? device : "Inconnu");
            history.setSuccess(success);
            loginHistoryRepository.save(history);
        });
    }
}
