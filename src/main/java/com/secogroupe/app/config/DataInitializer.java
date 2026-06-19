package com.secogroupe.app.config;

import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.secogroupe.app.entity.CompanySettings;
import com.secogroupe.app.entity.Permission;
import com.secogroupe.app.entity.Role;
import com.secogroupe.app.repository.CompanySettingsRepository;
import com.secogroupe.app.repository.PermissionRepository;
import com.secogroupe.app.repository.RoleRepository;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final RoleRepository roleRepo;
    private final PermissionRepository permRepo;
    private final CompanySettingsRepository companySettingsRepo;

    @Bean
    CommandLineRunner initData() {
        return args -> {
            // Employee permissions
            Permission createEmp  = upsert("CREATE_EMPLOYEE",  "Créer un employé",         "EMPLOYEES",   "CREATE");
            Permission readEmp    = upsert("READ_EMPLOYEE",    "Voir les employés",         "EMPLOYEES",   "READ");
            Permission updateEmp  = upsert("UPDATE_EMPLOYEE",  "Modifier un employé",       "EMPLOYEES",   "UPDATE");
            Permission deleteEmp  = upsert("DELETE_EMPLOYEE",  "Supprimer un employé",      "EMPLOYEES",   "DELETE");

            // User permissions
            Permission createUser  = upsert("CREATE_USER",    "Créer un utilisateur",       "USERS",       "CREATE");
            Permission readUser    = upsert("READ_USER",      "Voir les utilisateurs",      "USERS",       "READ");
            Permission updateUser  = upsert("UPDATE_USER",    "Modifier un utilisateur",    "USERS",       "UPDATE");
            Permission deleteUser  = upsert("DELETE_USER",    "Supprimer un utilisateur",   "USERS",       "DELETE");

            // Role permissions
            Permission createRole  = upsert("CREATE_ROLE",   "Créer un rôle",              "ROLES",       "CREATE");
            Permission readRole    = upsert("READ_ROLE",     "Voir les rôles",             "ROLES",       "READ");
            Permission updateRole  = upsert("UPDATE_ROLE",   "Modifier un rôle",           "ROLES",       "UPDATE");
            Permission deleteRole  = upsert("DELETE_ROLE",   "Supprimer un rôle",          "ROLES",       "DELETE");

            // Permission permissions
            Permission createPerm  = upsert("CREATE_PERMISSION", "Créer une permission",   "PERMISSIONS", "CREATE");
            Permission readPerm    = upsert("READ_PERMISSION",   "Voir les permissions",    "PERMISSIONS", "READ");
            Permission updatePerm  = upsert("UPDATE_PERMISSION", "Modifier une permission", "PERMISSIONS", "UPDATE");
            Permission deletePerm  = upsert("DELETE_PERMISSION", "Supprimer une permission","PERMISSIONS", "DELETE");

            // Quote permissions (demandes de devis)
            Permission readQuote   = upsert("READ_QUOTE",   "Voir les demandes de devis",      "QUOTES", "READ");
            Permission updateQuote = upsert("UPDATE_QUOTE", "Modifier une demande de devis",   "QUOTES", "UPDATE");
            Permission deleteQuote = upsert("DELETE_QUOTE", "Supprimer une demande de devis",  "QUOTES", "DELETE");

            Set<Permission> allPermissions = Set.of(
                    createEmp, readEmp, updateEmp, deleteEmp,
                    createUser, readUser, updateUser, deleteUser,
                    createRole, readRole, updateRole, deleteRole,
                    createPerm, readPerm, updatePerm, deletePerm,
                    readQuote, updateQuote, deleteQuote);

            // ADMIN role – all permissions
            Role admin = roleRepo.findByName("ADMIN").orElseGet(() -> {
                Role r = new Role("ADMIN");
                r.setDescription("Accès complet à toutes les fonctionnalités");
                return r;
            });
            if (admin.getDescription() == null) admin.setDescription("Accès complet à toutes les fonctionnalités");
            admin.setPermissions(allPermissions);
            roleRepo.save(admin);

            // USER role – read employees only
            Role user = roleRepo.findByName("USER").orElseGet(() -> {
                Role r = new Role("USER");
                r.setDescription("Accès en lecture seule aux employés");
                return r;
            });
            if (user.getDescription() == null) user.setDescription("Accès en lecture seule aux employés");
            user.setPermissions(Set.of(readEmp));
            roleRepo.save(user);

            // Default CompanySettings (only if not yet created)
            if (companySettingsRepo.count() == 0) {
                CompanySettings cs = new CompanySettings();
                cs.setCompanyName("SecoGroupe");
                cs.setCompanyEmail("contact@secogroupe.com");
                cs.setCompanyPhone("+33 1 23 45 67 89");
                cs.setWebsite("www.secogroupe.com");
                cs.setAddress("12 Rue de la Paix, 75001 Paris");
                cs.setTimezone("Europe/Paris");
                cs.setDateFormat("dd/MM/yyyy");
                cs.setLanguage("fr");
                companySettingsRepo.save(cs);
            }
        };
    }

    private Permission upsert(String name, String description, String module, String action) {
        Permission p = permRepo.findByName(name).orElseGet(() -> new Permission(name));
        if (p.getDescription() == null) p.setDescription(description);
        if (p.getModule() == null) p.setModule(module);
        if (p.getAction() == null) p.setAction(action);
        return permRepo.save(p);
    }
}
