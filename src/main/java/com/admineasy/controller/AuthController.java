// src/main/java/com/admineasy/controller/AuthController.java
package com.admineasy.controller;

import com.admineasy.model.User;
import com.admineasy.repository.UserRepository;
import com.admineasy.security.JwtUtils;
import com.admineasy.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepo;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String orgName = body.get("organizationName");
        String adminEmail = body.get("email");
        String password = body.get("password");
        try {
            User user = userService.registerOrganization(orgName, adminEmail, password);
            String token = jwtUtils.generateJwtToken(user);
            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "userId", user.getId(),
                    "orgId", user.getOrganization().getId()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");
        try {
            User user = userService.authenticate(email, password);
            String token = jwtUtils.generateJwtToken(user);
            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "userId", user.getId(),
                    "orgId", user.getOrganization().getId()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // On tente de récupérer depuis la base : si supprimé, userRepo.findById renverra Optional.empty()
        return userRepo.findById(currentUser.getId())
                .map(user -> ResponseEntity.ok(Map.of(
                        "userId", user.getId(),
                        "email", user.getEmail(),
                        "orgId", user.getOrganization().getId(),
                        "role", user.getRole().name()
                )))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Utilisateur introuvable")));
    }
}
