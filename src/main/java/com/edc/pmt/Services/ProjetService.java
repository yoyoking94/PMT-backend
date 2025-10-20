package com.edc.pmt.Services;

import com.edc.pmt.Entities.MembreProjet;
import com.edc.pmt.Entities.Projet;
import com.edc.pmt.Repository.MembreProjetRepository;
import com.edc.pmt.Repository.ProjetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import org.slf4j.*;

@Service
public class ProjetService {

    private final ProjetRepository projetRepository;
    private final MembreProjetRepository membreProjetRepository;
    private final Logger logger = LoggerFactory.getLogger(ProjetService.class);

    public ProjetService(ProjetRepository projetRepository, MembreProjetRepository membreProjetRepository) {
        this.projetRepository = projetRepository;
        this.membreProjetRepository = membreProjetRepository;
    }

    @Transactional
    public Projet creerProjet(Projet projet) {
        logger.debug("Création projet démarrée");
        Projet projetCree = projetRepository.save(projet);
        logger.debug("Projet créé avec id : {}", projetCree.getId());

        MembreProjet membreAdmin = new MembreProjet();
        membreAdmin.setProjetId(projetCree.getId());
        membreAdmin.setUtilisateurId(projetCree.getCreateurId());
        membreAdmin.setRole("administrateur");

        membreProjetRepository.save(membreAdmin);
        logger.debug("Membre administrateur ajouté pour utilisateur : {}", projetCree.getCreateurId());

        return projetCree;
    }

    public List<Projet> getProjetsByCreateur(Long createurId) {
        return projetRepository.findByCreateurId(createurId);
    }

    public List<Projet> getTousProjets() {
        return projetRepository.findAll();
    }

    public Optional<Projet> getProjetById(Long projetId) {
        return projetRepository.findById(projetId);
    }

    public boolean canEditProject(Long projetId, Long userId) {
        Optional<Projet> projetOptional = projetRepository.findById(projetId);
        if (projetOptional.isEmpty()) {
            return false;
        }
        Projet projet = projetOptional.get();

        if (userId.equals(projet.getCreateurId())) {
            return true;
        }

        return membreProjetRepository.existsByProjetIdAndUtilisateurId(projetId, userId);
    }

    @Transactional
    public Optional<Projet> updateProjet(Long projetId, Projet updatedProjet, Long userId) {
        if (!canEditProject(projetId, userId))
            return Optional.empty();

        return projetRepository.findById(projetId).map(projet -> {
            projet.setNom(updatedProjet.getNom());
            projet.setDescription(updatedProjet.getDescription());
            projet.setDateDebut(updatedProjet.getDateDebut());
            return projetRepository.save(projet);
        });
    }

    @Transactional
    public boolean deleteProjetIfAuthorized(Long projetId, Long userId) {
        if (canEditProject(projetId, userId)) {
            projetRepository.deleteById(projetId);
            return true;
        }
        return false;
    }
};