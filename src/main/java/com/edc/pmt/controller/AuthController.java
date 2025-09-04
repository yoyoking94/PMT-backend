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
@CrossOrigin(origins = "http://localhost:4200") // Autoriser ton frontend Angular (CORS)
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    // Endpoint inscription
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody User signupUser) {
        if (userRepository.findByUsername(signupUser.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Erreur : Le nom d'utilisateur existe déjà."));
        }
        userRepository.save(signupUser);
        return ResponseEntity.ok(Map.of("message", "Inscription réussie"));
    }

    // Endpoint connexion
    @PostMapping("/signin")
    public ResponseEntity<?> signin(@RequestBody User loginUser) {
        Optional<User> userOpt = userRepository.findByUsername(loginUser.getUsername());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("message", "Nom d'utilisateur ou mot de passe incorrect"));
        }
        User user = userOpt.get();
        if (!user.getPassword().equals(loginUser.getPassword())) {
            return ResponseEntity.status(401).body(Map.of("message", "Nom d'utilisateur ou mot de passe incorrect"));
        }
        return ResponseEntity.ok(Map.of("message", "Connexion réussie"));
    }
}
