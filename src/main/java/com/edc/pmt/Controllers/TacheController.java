package com.edc.pmt.Controllers;

import com.edc.pmt.Entities.Tache;
import com.edc.pmt.Services.TacheService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.sql.Date;
import java.util.Map;

@RestController
@RequestMapping("/api/taches")
@CrossOrigin(origins = "http://localhost:4200")
public class TacheController {
    private final TacheService tacheService;

    public TacheController(TacheService tacheService) {
        this.tacheService = tacheService;
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
                tache.setDateEcheance(Date.valueOf(body.get("dateEcheance").toString()));
            }

            Object prioriteObj = body.get("priorite");
            if (prioriteObj == null)
                return ResponseEntity.badRequest().body("priorite manquante");
            tache.setPriorite(Tache.Priorite.valueOf(prioriteObj.toString()));

            tache.setStatut(Tache.Statut.a_faire); // valeur par défaut comme avant

            Object createurIdObj = body.get("createurId");
            if (createurIdObj != null) {
                tache.setCreateurId(Long.valueOf(createurIdObj.toString()));
            }

            Long membreId = null;
            if (body.get("membreId") != null && !body.get("membreId").toString().isEmpty()) {
                membreId = Long.valueOf(body.get("membreId").toString());
            }

            Tache saved = tacheService.createTacheEtAssigner(tache, membreId);
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Erreur interne sur serveur");
        }
    }

}
