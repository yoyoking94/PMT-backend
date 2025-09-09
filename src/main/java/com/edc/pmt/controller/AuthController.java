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
@CrossOrigin(origins = "http://localhost:4200") // Autoriser appels Angular
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    // Inscription
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody User newUser) {
        if (userRepository.findByEmail(newUser.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Erreur : Le nom d'utilisateur existe déjà.");
        }
        userRepository.save(newUser);
        return ResponseEntity.ok("Inscription réussie");
    }

    // Connexion
    @PostMapping("/signin")
    public ResponseEntity<?> signin(@RequestBody User loginUser) {
        Optional<User> userOpt = userRepository.findByEmail(loginUser.getUsername());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body("Nom d'utilisateur ou mot de passe incorrect");
        }

        User user = userOpt.get();
        if (!user.getPassword().equals(loginUser.getPassword())) {
            return ResponseEntity.status(401).body("Nom d'utilisateur ou mot de passe incorrect");
        }

        // Retourner id et username pour le frontend
        return ResponseEntity.ok(Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "message", "Connexion réussie"));
    }
}
