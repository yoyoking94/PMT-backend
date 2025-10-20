package com.edc.pmt.Controllers;

import com.edc.pmt.Entities.HistoriqueModification;
import com.edc.pmt.Entities.Tache;
import com.edc.pmt.Services.HistoriqueModificationService;
import com.edc.pmt.Services.TacheService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/taches")
@CrossOrigin(origins = "http://localhost:4200")
public class TacheController {

    private final TacheService tacheService;
    private final HistoriqueModificationService historiqueService;

    public TacheController(TacheService tacheService, HistoriqueModificationService historiqueService) {
        this.tacheService = tacheService;
        this.historiqueService = historiqueService;
    }

    @GetMapping("/projet/{projetId}")
    public ResponseEntity<List<Tache>> getTaches(@PathVariable Long projetId) {
        return ResponseEntity.ok(tacheService.getTachesByProjet(projetId));
    }

    @PostMapping("/create")
    public ResponseEntity<?> creerTache(@RequestBody Map<String, Object> body) {
        try {
            Tache tache = new Tache();

            Object projetIdObj = body.get("projetId");
            if (projetIdObj == null)
                return ResponseEntity.badRequest().body("projetId manquant");
            tache.setProjetId(Long.valueOf(projetIdObj.toString()));

            Object nomObj = body.get("nom");
            if (nomObj == null)
                return ResponseEntity.badRequest().body("nom manquant");
            tache.setNom(nomObj.toString());

            tache.setDescription(body.get("description") != null ? body.get("description").toString() : null);

            if (body.get("dateEcheance") != null) {
                tache.setDateEcheance(java.sql.Date.valueOf(body.get("dateEcheance").toString()));
            }

            Object prioriteObj = body.get("priorite");
            if (prioriteObj == null)
                return ResponseEntity.badRequest().body("priorite manquante");
            tache.setPriorite(Tache.Priorite.valueOf(prioriteObj.toString()));

            tache.setStatut(Tache.Statut.a_faire);

            Object createurIdObj = body.get("createurId");
            if (createurIdObj != null)
                tache.setCreateurId(Long.valueOf(createurIdObj.toString()));

            Long membreId = null;
            if (body.get("membreId") != null && !body.get("membreId").toString().isEmpty()) {
                membreId = Long.valueOf(body.get("membreId").toString());
            }

            Tache saved = tacheService.createTacheEtAssigner(tache, membreId);
            return ResponseEntity.ok(saved);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Erreur interne sur serveur");
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateTache(
            @PathVariable Long id,
            @RequestBody Tache tache,
            @RequestParam Long userId,
            @RequestParam String userRole) {
        Optional<Tache> updated = tacheService.updateTache(id, tache, userId, userRole);
        if (updated.isPresent()) {
            return ResponseEntity.ok(updated.get());
        } else {
            return ResponseEntity.status(403).body("Accès refusé ou permission insuffisante");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tache> getTacheById(@PathVariable Long id) {
        return tacheService.getTacheById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{tacheId}/historique")
    public ResponseEntity<List<HistoriqueModification>> getHistorique(@PathVariable Long tacheId) {
        List<HistoriqueModification> historique = historiqueService.getHistoriqueParTache(tacheId);
        return ResponseEntity.ok(historique);
    }
}
