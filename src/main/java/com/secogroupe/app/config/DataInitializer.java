package com.secogroupe.app.config;


import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.secogroupe.app.entity.Permission;
import com.secogroupe.app.entity.Role;
import com.secogroupe.app.repository.PermissionRepository;
import com.secogroupe.app.repository.RoleRepository;

import java.util.List;
import java.util.Set;

import org.springframework.boot.CommandLineRunner;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final RoleRepository roleRepo;
    private final PermissionRepository permRepo;

    @Bean
CommandLineRunner initData() {
    return args -> {

        Permission create = new Permission("CREATE_EMPLOYEE");

        Permission read = new Permission("READ_EMPLOYEE");

        Permission delete = new Permission("DELETE_EMPLOYEE");

        permRepo.saveAll(List.of(create, read, delete));

        Role admin = new Role("ADMIN");
        admin.setPermissions(Set.of(create, read, delete));

        Role user = new Role("USER");
        user.setPermissions(Set.of(read));

        roleRepo.saveAll(List.of(admin, user));
    };
}
    
}
