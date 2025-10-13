package com.edc.pmt.Services;

import com.edc.pmt.Entities.Tache;
import com.edc.pmt.Entities.AssignationTache;
import com.edc.pmt.Entities.Utilisateur;
import com.edc.pmt.Repository.TacheRepository;
import com.edc.pmt.Repository.AssignationTacheRepository;
import com.edc.pmt.Repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TacheService {

    private final TacheRepository tacheRepo;
    private final AssignationTacheRepository assignationRepo;
    private final UtilisateurRepository utilisateurRepo;
    private final EmailService emailService;

    @Autowired
    public TacheService(
            TacheRepository tacheRepo,
            AssignationTacheRepository assignationRepo,
            UtilisateurRepository utilisateurRepo,
            EmailService emailService) {
        this.tacheRepo = tacheRepo;
        this.assignationRepo = assignationRepo;
        this.utilisateurRepo = utilisateurRepo;
        this.emailService = emailService;
    }

    public List<Tache> getTachesByProjet(Long projetId) {
        List<Tache> taches = tacheRepo.findByProjetId(projetId);
        for (Tache tache : taches) {
            List<AssignationTache> assigns = assignationRepo.findByTacheId(tache.getId());
            if (!assigns.isEmpty()) {
                tache.setMembreId(assigns.get(0).getUtilisateurId());
            }
        }
        return taches;
    }

    public Optional<Tache> getTacheById(Long id) {
        Optional<Tache> tacheOpt = tacheRepo.findById(id);
        tacheOpt.ifPresent(tache -> {
            List<AssignationTache> assigns = assignationRepo.findByTacheId(tache.getId());
            if (!assigns.isEmpty()) {
                tache.setMembreId(assigns.get(0).getUtilisateurId());
            }
        });
        return tacheOpt;
    }

    @Transactional
    public Tache createTacheEtAssigner(Tache tache, Long membreId) {
        Tache saved = tacheRepo.save(tache);
        if (membreId != null) {
            AssignationTache newAssign = new AssignationTache();
            newAssign.setTacheId(saved.getId());
            newAssign.setUtilisateurId(membreId);
            assignationRepo.save(newAssign);
            saved.setMembreId(membreId);

            // Envoi d'un mail à l'utilisateur assigné
            Optional<Utilisateur> utilisateurOpt = utilisateurRepo.findById(membreId);
            utilisateurOpt.ifPresent(utilisateur -> {
                String email = utilisateur.getEmail();
                String sujet = "Nouvelle tâche assignée : " + tache.getNom();
                String corps = "Bonjour,\n\nVous avez été assigné(e) à la tâche suivante : " + tache.getNom()
                        + "\n\nDescription : " + tache.getDescription()
                        + "\n\nMerci de prendre en compte cette assignation.\n\nCordialement,\nL'équipe PMT";
                emailService.sendSimpleMessage(email, sujet, corps);
            });
        }
        return saved;
    }

    @Transactional
    public Optional<Tache> updateTache(Long id, Tache updated, Long userId, String userRole) {
        Optional<Tache> optionalTache = tacheRepo.findById(id);
        if (optionalTache.isEmpty())
            return Optional.empty();

        Tache tache = optionalTache.get();

        if (!canEditTache(userRole))
            return Optional.empty();

        if ("administrateur".equalsIgnoreCase(userRole)) {
            tache.setNom(updated.getNom());
            tache.setDescription(updated.getDescription());
            tache.setDateEcheance(updated.getDateEcheance());
            tache.setPriorite(updated.getPriorite());
            tache.setStatut(updated.getStatut());

            Long newMembreId = updated.getMembreId();
            List<AssignationTache> assigns = assignationRepo.findByTacheId(id);

            if (!assigns.isEmpty()) {
                AssignationTache assign = assigns.get(0);
                if (newMembreId == null) {
                    assignationRepo.delete(assign);
                    tache.setMembreId(null);
                } else if (!newMembreId.equals(assign.getUtilisateurId())) {
                    assign.setUtilisateurId(newMembreId);
                    assignationRepo.save(assign);
                    tache.setMembreId(newMembreId);

                    // Envoi mail si la tâche est réassignée à un nouvel utilisateur
                    Optional<Utilisateur> utilisateurOpt = utilisateurRepo.findById(newMembreId);
                    utilisateurOpt.ifPresent(utilisateur -> {
                        String email = utilisateur.getEmail();
                        String sujet = "Tâche réassignée : " + tache.getNom();
                        String corps = "Bonjour,\n\nVous avez été réassigné(e) à la tâche suivante : " + tache.getNom()
                                + "\n\nDescription : " + tache.getDescription()
                                + "\n\nMerci de prendre en compte cette assignation.\n\nCordialement,\nL'équipe PMT";
                        emailService.sendSimpleMessage(email, sujet, corps);
                    });
                }
            } else if (newMembreId != null) {
                AssignationTache newAssign = new AssignationTache();
                newAssign.setTacheId(id);
                newAssign.setUtilisateurId(newMembreId);
                assignationRepo.save(newAssign);
                tache.setMembreId(newMembreId);

                // Envoi mail à nouveau membre assigné
                Optional<Utilisateur> utilisateurOpt = utilisateurRepo.findById(newMembreId);
                utilisateurOpt.ifPresent(utilisateur -> {
                    String email = utilisateur.getEmail();
                    String sujet = "Nouvelle tâche assignée : " + tache.getNom();
                    String corps = "Bonjour,\n\nVous avez été assigné(e) à la tâche suivante : " + tache.getNom()
                            + "\n\nDescription : " + tache.getDescription()
                            + "\n\nMerci de prendre en compte cette assignation.\n\nCordialement,\nL'équipe PMT";
                    emailService.sendSimpleMessage(email, sujet, corps);
                });
            }
        } else if ("membre".equalsIgnoreCase(userRole)) {
            tache.setStatut(updated.getStatut());
        } else {
            // Observateur ne peut rien modifier
            return Optional.empty();
        }

        tacheRepo.save(tache);
        return Optional.of(tache);
    }

    private boolean canEditTache(String userRole) {
        return "administrateur".equalsIgnoreCase(userRole) || "membre".equalsIgnoreCase(userRole);
    }

    public void deleteTache(Long id) {
        tacheRepo.deleteById(id);
    }
}
