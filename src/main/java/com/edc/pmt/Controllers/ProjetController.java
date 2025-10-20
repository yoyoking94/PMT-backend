package com.edc.pmt.Controllers;

import com.edc.pmt.Entities.Projet;
import com.edc.pmt.Services.ProjetService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/projects")
@CrossOrigin(origins = "http://localhost:4200")
public class ProjetController {

    private final ProjetService projetService;

    public ProjetController(ProjetService projetService) {
        this.projetService = projetService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createProject(@RequestBody Projet projet) {
        try {
            Projet created = projetService.creerProjet(projet);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erreur lors de la création du projet");
        }
    }

    @GetMapping("/myprojects/{userId}")
    public ResponseEntity<List<Projet>> getMyProjects(@PathVariable Long userId) {
        return ResponseEntity.ok(projetService.getProjetsByCreateur(userId));
    }

    @GetMapping("/all")
    public ResponseEntity<List<Projet>> getAllProjects() {
        return ResponseEntity.ok(projetService.getTousProjets());
    }

    @DeleteMapping("/delete/{projetId}/{userId}")
    public ResponseEntity<?> deleteProject(@PathVariable Long projetId, @PathVariable Long userId) {
        if (projetService.deleteProjetIfAuthorized(projetId, userId)) {
            return ResponseEntity.ok(Map.of("message", "Projet supprimé"));
        } else {
            return ResponseEntity.status(403).body(Map.of("error", "Accès refusé"));
        }
    }

    @GetMapping("/{projetId}")
    public ResponseEntity<?> getProjectById(@PathVariable Long projetId) {
        Optional<Projet> projet = projetService.getProjetById(projetId);
        return projet.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateProject(@PathVariable Long id, @RequestBody Projet projet,
            @RequestParam Long userId) {
        Optional<Projet> updated = projetService.updateProjet(id, projet, userId);
        if (updated.isPresent()) {
            return ResponseEntity.ok(updated.get());
        } else {
            return ResponseEntity.status(403).body("Accès refusé");
        }
    }
};