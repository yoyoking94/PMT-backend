package com.edc.pmt.controller;

import com.edc.pmt.entity.User;
import com.edc.pmt.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    // Inscription utilisateur
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody User newUser) {
        if (userRepository.findByEmail(newUser.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Erreur : L'email existe déjà.");
        }
        userRepository.save(newUser);
        return ResponseEntity.ok("Inscription réussie");
    }

    // Connexion utilisateur
    @PostMapping("/signin")
    public ResponseEntity<?> signin(@RequestBody User loginUser) {
        Optional<User> userOpt = userRepository.findByEmail(loginUser.getEmail());
        if (userOpt.isEmpty() || !userOpt.get().getPassword().equals(loginUser.getPassword())) {
            return ResponseEntity.status(401).body("Email ou mot de passe incorrect");
        }
        User user = userOpt.get();
        return ResponseEntity.ok(Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail(),
                "message", "Connexion réussie"));
    }
};