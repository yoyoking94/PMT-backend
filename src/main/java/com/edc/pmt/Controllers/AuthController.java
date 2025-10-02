package com.edc.pmt.Controllers;

import com.edc.pmt.Entities.Utilisateur;
import com.edc.pmt.Services.UtilisateurService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    private final UtilisateurService utilisateurService;

    public AuthController(UtilisateurService utilisateurService) {
        this.utilisateurService = utilisateurService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody Utilisateur utilisateur) {
        System.out.println("Tentative d'inscription pour email : " + utilisateur.getEmail());
        try {
            if (utilisateurService.findByEmail(utilisateur.getEmail()).isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Cet email est déjà utilisé."));
            }
            Utilisateur savedUser = utilisateurService.signup(utilisateur);
            System.out.println("Utilisateur enregistré : " + savedUser.getId());
            return ResponseEntity.ok(Map.of("message", "Utilisateur enregistré avec succès", "user", savedUser));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Erreur serveur lors de l'inscription"));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody Utilisateur utilisateur) {
        try {
            Optional<Utilisateur> user = utilisateurService.signin(utilisateur.getEmail(), utilisateur.getPassword());
            if (user.isPresent()) {
                // Optionnel : ne jamais retourner le mot de passe en réponse
                Utilisateur loggedUser = user.get();
                loggedUser.setPassword(null);
                return ResponseEntity.ok(Map.of("message", "Connexion réussie", "user", loggedUser));
            } else {
                return ResponseEntity.status(401).body(Map.of("error", "Email ou mot de passe invalide"));
            }
        } catch (Exception e) {
            e.printStackTrace(); // Log technique
            return ResponseEntity.status(500).body(Map.of("error", "Erreur serveur lors de la connexion"));
        }
    }

};