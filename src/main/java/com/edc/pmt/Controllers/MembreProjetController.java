package com.edc.pmt.Controllers;

import com.edc.pmt.Entities.MembreProjet;
import com.edc.pmt.Services.MembreProjetService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/membres")
@CrossOrigin(origins = "http://localhost:4200")
public class MembreProjetController {

    private final MembreProjetService membreProjetService;

    public MembreProjetController(MembreProjetService membreProjetService) {
        this.membreProjetService = membreProjetService;
    }

    @GetMapping("/projet/{projetId}")
    public ResponseEntity<List<MembreProjet>> getMembresParProjet(@PathVariable Long projetId) {
        return ResponseEntity.ok(membreProjetService.getMembresByProjet(projetId));
    }

    // Ajout de membre avec vérification rôle admin via userId param
    @PostMapping("/add")
    public ResponseEntity<?> addMembreByEmail(
            @RequestParam Long projetId,
            @RequestParam String email,
            @RequestParam String role,
            @RequestParam Long userId // doit être l’id de l’utilisateur connecté
    ) {
        try {
            MembreProjet membre = membreProjetService.addMembreByEmail(projetId, email, role, userId);
            return ResponseEntity.ok(membre);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/delete/{membreId}")
    public ResponseEntity<?> removeMembre(@PathVariable Long membreId) {
        membreProjetService.removeMembre(membreId);
        return ResponseEntity.ok().build();
    }
}
