package com.edc.pmt.Services;

import com.edc.pmt.Entities.MembreProjet;
import com.edc.pmt.Repository.MembreProjetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MembreProjetService {

    private final MembreProjetRepository membreProjetRepository;
    private final UtilisateurService utilisateurService;

    public MembreProjetService(MembreProjetRepository membreProjetRepository, UtilisateurService utilisateurService) {
        this.membreProjetRepository = membreProjetRepository;
        this.utilisateurService = utilisateurService;
    }

    public List<MembreProjet> getMembresByProjet(Long projetId) {
        return membreProjetRepository.findByProjetId(projetId);
    }

    public boolean isAdmin(Long projetId, Long userId) {
        List<MembreProjet> membres = membreProjetRepository.findByProjetId(projetId);
        for (MembreProjet membre : membres) {
            System.out.printf("Vérif admin: projet=%d, user=%d, role=%s\n",
                    projetId, membre.getUtilisateurId(), membre.getRole());
        }
        return membres.stream()
                .anyMatch(m -> m.getUtilisateurId().equals(userId) && "administrateur".equalsIgnoreCase(m.getRole()));
    }

    @Transactional
    public MembreProjet addMembreByEmail(Long projetId, String email, String role, Long demandeurId) {
        if (!isAdmin(projetId, demandeurId)) {
            throw new RuntimeException("Seuls les administrateurs peuvent ajouter des membres.");
        }
        Long userId = utilisateurService.getUserIdByEmail(email);
        if (userId == null) {
            throw new RuntimeException("Utilisateur non trouvé");
        }
        if (membreProjetRepository.existsByProjetIdAndUtilisateurId(projetId, userId)) {
            throw new RuntimeException("Membre déjà présent dans ce projet");
        }
        MembreProjet membre = new MembreProjet(null, projetId, userId, role);
        return membreProjetRepository.save(membre);
    }

    @Transactional
    public void removeMembre(Long membreId) {
        membreProjetRepository.deleteById(membreId);
    }
}