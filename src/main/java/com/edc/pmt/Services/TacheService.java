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
    private final HistoriqueModificationService historiqueService;

    @Autowired
    public TacheService(
            TacheRepository tacheRepo,
            AssignationTacheRepository assignationRepo,
            UtilisateurRepository utilisateurRepo,
            EmailService emailService,
            HistoriqueModificationService historiqueService) {
        this.tacheRepo = tacheRepo;
        this.assignationRepo = assignationRepo;
        this.utilisateurRepo = utilisateurRepo;
        this.emailService = emailService;
        this.historiqueService = historiqueService;
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

        // Enregistrer les changements dans l'historique si différent
        historiqueService.enregistrerSiModifie(id, userId, "nom", tache.getNom(), updated.getNom());
        historiqueService.enregistrerSiModifie(id, userId, "description", tache.getDescription(),
                updated.getDescription());
        historiqueService.enregistrerSiModifie(id, userId, "priorite",
                tache.getPriorite().toString(), updated.getPriorite().toString());
        historiqueService.enregistrerSiModifie(id, userId, "statut",
                tache.getStatut().toString(), updated.getStatut().toString());
        if (tache.getDateEcheance() != null || updated.getDateEcheance() != null) {
            String oldDate = tache.getDateEcheance() != null ? tache.getDateEcheance().toString() : null;
            String newDate = updated.getDateEcheance() != null ? updated.getDateEcheance().toString() : null;
            historiqueService.enregistrerSiModifie(id, userId, "dateEcheance", oldDate, newDate);
        }

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
