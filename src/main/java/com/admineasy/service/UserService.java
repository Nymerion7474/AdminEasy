// src/main/java/com/admineasy/service/UserService.java
package com.admineasy.service;

import com.admineasy.model.Organization;
import com.admineasy.model.User;
import com.admineasy.repository.OrganizationRepository;
import com.admineasy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private OrganizationRepository orgRepo;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * Crée une nouvelle organisation et l’admin initial pour cette org.
     *
     * @param orgName     Nom de la nouvelle organisation
     * @param adminEmail  E-mail de l’admin
     * @param rawPassword Mot de passe en clair (sera hashé)
     * @return l’utilisateur admin créé
     */
    public User registerOrganization(String orgName, String adminEmail, String rawPassword) {
        // 1. Créer l’organisation
        Organization org = new Organization();
        org.setName(orgName);
        org.setAdminEmail(adminEmail);
        // Par défaut, on laisse tous les flags de rappel à false
        org = orgRepo.save(org);

        // 2. Créer l’utilisateur admin
        User admin = new User();
        admin.setEmail(adminEmail);
        admin.setPasswordHash(passwordEncoder.encode(rawPassword));
        admin.setOrganization(org);
        admin.setRole(User.Role.ADMIN);
        return userRepo.save(admin);
    }

    /**
     * Authentifie un utilisateur.
     *
     * @param email       e-mail login
     * @param rawPassword mot de passe en clair
     * @return l’utilisateur si validé, sinon exception RuntimeException
     */
    public User authenticate(String email, String rawPassword) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new RuntimeException("Mot de passe incorrect");
        }
        return user;
    }
}
